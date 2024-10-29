package nbc_final.gathering.domain.event.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import nbc_final.gathering.common.exception.ResponseCode;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.domain.comment.dto.response.CommentResponseDto;
import nbc_final.gathering.domain.comment.repository.CommentRepository;
import nbc_final.gathering.domain.event.dto.ParticipantResponseDto;
import nbc_final.gathering.domain.event.dto.request.EventCreateRequestDto;
import nbc_final.gathering.domain.event.dto.request.EventUpdateRequestDto;
import nbc_final.gathering.domain.event.dto.response.EventListResponseDto;
import nbc_final.gathering.domain.event.dto.response.EventResponseDto;
import nbc_final.gathering.domain.event.dto.response.EventUpdateResponseDto;
import nbc_final.gathering.domain.event.entity.Event;
import nbc_final.gathering.domain.event.entity.Participant;
import nbc_final.gathering.domain.event.repository.EventRepository;
import nbc_final.gathering.domain.event.repository.EventRepositoryCustom;
import nbc_final.gathering.domain.event.repository.ParticipantRepository;
import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.gathering.repository.GatheringRepository;
import nbc_final.gathering.domain.user.entity.User;
import nbc_final.gathering.domain.user.enums.UserRole;
import nbc_final.gathering.domain.user.repository.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import nbc_final.gathering.domain.comment.entity.Comment;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final ParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EventRepositoryCustom eventRepositoryCustom;
    private final GatheringRepository gatheringRepository;
    private final CommentRepository commentRepository;
    private final RedisTemplate redisTemplate;

    // 이벤트 생성 (권한: 소모임 멤버 또는 어드민)
    @Transactional
    public EventResponseDto createEvent(Long userId, Long gatheringId, EventCreateRequestDto requestDto) {
        checkAdminOrGatheringMemberForCreation(userId, gatheringId);

        Gathering gathering = getGatheringOrThrow(gatheringId);
        User user = getUserOrThrow(userId);

        Event event = Event.of(requestDto.getTitle(), requestDto.getDescription(), requestDto.getDate(),
                requestDto.getLocation(), requestDto.getMaxParticipants(), gathering, user);
        eventRepository.save(event);

        // 이벤트 생성자는 자동으로 참가
        Participant participant = Participant.of(event, user);
        event.addParticipant(participant);

        return EventResponseDto.of(event, userId);
    }

    // 이벤트 수정 (권한: 이벤트 생성자만 가능, 어드민 불가능)
    @Transactional
    public EventUpdateResponseDto updateEvent(Long userId, Long gatheringId, Long eventId, EventUpdateRequestDto requestDto) {
        checkEventCreatorForUpdate(userId, eventId, gatheringId);

        Event event = getEventOrThrow(eventId);

        if (event.getCurrentParticipants() > requestDto.getMaxParticipants()) {
            throw new ResponseCodeException(ResponseCode.INVALID_MAX_PARTICIPANTS);
        }

        event.updateEvent(requestDto.getTitle(), requestDto.getDescription(), requestDto.getDate(),
                requestDto.getLocation(), requestDto.getMaxParticipants());
        return EventUpdateResponseDto.of(event);
    }

    // 이벤트 다건 조회 (권한: 소모임 멤버 또는 어드민)
    public EventListResponseDto getAllEvents(Long userId, Long gatheringId) throws JsonProcessingException {
        checkAdminOrGatheringMemberForView(userId, gatheringId);

        String cacheKey = "userEvent:" + userId;
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // LocalDateTime 지원

        // Redis 캐시 조회
        String cacheData = valueOperations.get(cacheKey);
        if (cacheData != null ) {
            // JSON 형식으로 저장된 데이터를 EventListResponseDto 역직렬화
            return new ObjectMapper().readValue(cacheData, EventListResponseDto.class);
        }

        List<Event> events = eventRepository.findAllByGatheringId(gatheringId);

        // EventListResponseDto 객체 생성
        EventListResponseDto eventListResponseDto = EventListResponseDto.of(events, userId);

        // Redis에 캐싱
        String jsonValue = objectMapper.writeValueAsString(eventListResponseDto);
        valueOperations.set(cacheKey, jsonValue, 10, TimeUnit.MINUTES);

        return eventListResponseDto;


    }

    // 이벤트 단건 조회 (권한: 소모임 멤버 또는 어드민)
    public EventResponseDto getEvent(Long userId, Long gatheringId, Long eventId) {
        checkAdminOrGatheringMemberForView(userId, gatheringId);

        Event event = getEventOrThrow(eventId);

        List<Comment> comments = commentRepository.findByEventId(eventId);
        List<CommentResponseDto> commentResponseDtos = comments.stream()
                .map(CommentResponseDto::of)
                .collect(Collectors.toList());

        return EventResponseDto.of(event, userId, commentResponseDtos);
    }

    // 이벤트 삭제 (권한: 이벤트 생성자 또는 어드민)
    @Transactional
    public void deleteEvent(Long userId, Long gatheringId, Long eventId) {
        checkAdminOrEventCreatorForDeletion(userId, eventId, gatheringId);

        Event event = getEventOrThrow(eventId);

        eventRepository.delete(event);
    }

    // 이벤트 참가 (권한: 어드민 불가, 이벤트 생성자 불가, 게더링 멤버 가능)
    @Transactional
    public void participateInEvent(Long userId, Long gatheringId, Long eventId) {
        User user = getUserOrThrow(userId);
        Event event = getEventOrThrow(eventId);

        validateParticipantConditions(user, event, userId);

        if (event.getCurrentParticipants() >= event.getMaxParticipants()) {
            throw new ResponseCodeException(ResponseCode.PARTICIPANT_LIMIT_EXCEEDED);
        }

        checkAdminOrGatheringMemberForCreation(userId, gatheringId);

        Participant participant = Participant.of(event, user);
        event.addParticipant(participant);
    }

    // 이벤트 참가 취소 (권한: 어드민 불가, 이벤트 생성자 불가)
    @Transactional
    public void cancelParticipation(Long userId, Long gatheringId, Long eventId) {
        User user = getUserOrThrow(userId);

        if (user.getUserRole().equals(UserRole.ROLE_ADMIN)) {
            throw new ResponseCodeException(ResponseCode.ADMIN_CANNOT_CANCEL_PARTICIPATION);
        }

        Event event = getEventOrThrow(eventId);

        if (event.getUser().getId().equals(userId)) {
            throw new ResponseCodeException(ResponseCode.EVENT_CREATOR_CANNOT_CANCEL);
        }

        Participant participant = participantRepository.findByEventAndUserId(event, userId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_PARTICIPATED));

        event.removeParticipant(participant);
    }

    // 이벤트 참가자 조회 (권한: 소모임 멤버 또는 어드민)
    @Transactional(readOnly = true)
    public List<ParticipantResponseDto> getParticipants(Long userId, Long gatheringId, Long eventId) {
        checkAdminOrGatheringMemberForView(userId, gatheringId);

        Event event = getEventOrThrow(eventId);

        List<Participant> participants = participantRepository.findAllByEvent(event);

        return participants.stream()
                .map(ParticipantResponseDto::from)
                .collect(Collectors.toList());
    }


    // 공통 권한 검증 (생성 권한: 어드민 또는 게더링 멤버)
    private void checkAdminOrGatheringMemberForCreation(Long userId, Long gatheringId) {
        checkPermission(userId, gatheringId, true, true);
    }


    // 공통 권한 검증 (권한: 어드민 또는 게더링 멤버)
    private void checkPermission(Long userId, Long gatheringId, boolean forAdmin, boolean forMember) {
        User user = getUserOrThrow(userId);

        boolean isAdmin = user.getUserRole().equals(UserRole.ROLE_ADMIN);
        boolean isGatheringMember = eventRepositoryCustom.isUserInGathering(gatheringId, userId);

        if ((forAdmin && isAdmin) || (forMember && isGatheringMember)) {
            return;
        }

        throw new ResponseCodeException(ResponseCode.FORBIDDEN);
    }

    // 조회 권한 (어드민 또는 게더링 멤버)
    private void checkAdminOrGatheringMemberForView(Long userId, Long gatheringId) {
        checkPermission(userId, gatheringId, true, true);
    }

    // 삭제 권한 (어드민 또는 이벤트 생성자)
    private void checkAdminOrEventCreatorForDeletion(Long userId, Long eventId, Long gatheringId) {
        Event event = getEventOrThrow(eventId);
        User user = getUserOrThrow(userId);

        boolean isAdmin = user.getUserRole().equals(UserRole.ROLE_ADMIN);
        boolean isEventCreator = event.getUser().getId().equals(userId);

        if (!isAdmin && !isEventCreator) {
            throw new ResponseCodeException(ResponseCode.FORBIDDEN);
        }
    }

    // 수정 권한 (이벤트 생성자만 가능)
    private void checkEventCreatorForUpdate(Long userId, Long eventId, Long gatheringId) {
        Event event = getEventOrThrow(eventId);

        boolean isEventCreator = event.getUser().getId().equals(userId);

        if (!isEventCreator) {
            throw new ResponseCodeException(ResponseCode.FORBIDDEN);
        }
    }

    // 참가자 관련 검증 (어드민 불가, 이벤트 생성자 불가, 이미 참가 여부 등)
    private void validateParticipantConditions(User user, Event event, Long userId) {
        if (user.getUserRole().equals(UserRole.ROLE_ADMIN)) {
            throw new ResponseCodeException(ResponseCode.ADMIN_CANNOT_PARTICIPATE);
        }

        if (event.getUser().getId().equals(userId)) {
            throw new ResponseCodeException(ResponseCode.EVENT_CREATOR_CANNOT_PARTICIPATE);
        }

        if (participantRepository.findByEventAndUserId(event, userId).isPresent()) {
            throw new ResponseCodeException(ResponseCode.ALREADY_PARTICIPATED);
        }
    }

    private Gathering getGatheringOrThrow(Long gatheringId) {
        return gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GATHERING));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));
    }

    private Event getEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT));
    }
}
