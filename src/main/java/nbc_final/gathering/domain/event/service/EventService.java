package nbc_final.gathering.domain.event.service;

import lombok.RequiredArgsConstructor;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import nbc_final.gathering.domain.comment.entity.Comment;

import java.util.List;
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

    // 이벤트 생성 (권한: 소모임 멤버 또는 어드민)
    @Transactional
    public EventResponseDto createEvent(Long userId, Long gatheringId, EventCreateRequestDto requestDto) {
        checkAdminOrGatheringMember(userId, gatheringId);

        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GATHERING));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));

        Event event = Event.of(requestDto.getTitle(), requestDto.getDescription(), requestDto.getDate(), requestDto.getLocation(), requestDto.getMaxParticipants(), gathering, user
        );
        eventRepository.save(event);

        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));
        Participant participant = Participant.of(event, creator);
        event.addParticipant(participant);

        return EventResponseDto.of(event, userId);
    }


    // 이벤트 수정 (권한: 소모임 생성자 또는 이벤트 생성자)
    @Transactional
    public EventUpdateResponseDto updateEvent(Long userId, Long gatheringId, Long eventId, EventUpdateRequestDto requestDto) {
        checkGatheringCreatorOrEventCreator(userId, eventId, gatheringId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT));
        if (event.getCurrentParticipants() > requestDto.getMaxParticipants()) {
            throw new ResponseCodeException(ResponseCode.INVALID_MAX_PARTICIPANTS);
        }

        event.updateEvent(requestDto.getTitle(), requestDto.getDescription(), requestDto.getDate(), requestDto.getLocation(), requestDto.getMaxParticipants());
        return EventUpdateResponseDto.of(event);
    }

    // 이벤트 다건 조회 (권한: 소모임 멤버 또는 어드민)
    public EventListResponseDto getAllEvents(Long userId, Long gatheringId) {
        checkAdminOrGatheringMember(userId, gatheringId);
        List<Event> events = eventRepository.findAllByGatheringId(gatheringId);
        return EventListResponseDto.of(events, userId);
    }

    // 이벤트 단건 조회 (권한: 소모임 멤버 또는 어드민)
    public EventResponseDto getEvent(Long userId, Long gatheringId, Long eventId) {

        checkAdminOrGatheringMember(userId, gatheringId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT));

        List<Comment> comments = commentRepository.findByEventId(eventId);
        List<CommentResponseDto> commentResponseDtos = comments.stream()
                .map(CommentResponseDto::of)
                .collect(Collectors.toList());

        return EventResponseDto.of(event, userId, commentResponseDtos);
    }

    // 검증 권한: 어드민, 이벤트 생성자, 소모임 생성자
    @Transactional
    public void deleteEvent(Long userId, Long gatheringId, Long eventId) {

        checkAdminOrEventCreatorOrGatheringCreator(userId, eventId, gatheringId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT));

        eventRepository.delete(event);
    }

    // 이벤트 참가 (권한: 소모임 멤버 또는 어드민)
    @Transactional
    public void participateInEvent(Long userId, Long gatheringId, Long eventId) {
        // 공통 권한 검증
        checkAdminOrGatheringMember(userId, gatheringId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT));

        // 이미 참가한 유저인지 확인
        boolean alreadyParticipated = participantRepository.findByEventAndUserId(event, userId).isPresent();
        if (alreadyParticipated) {
            throw new ResponseCodeException(ResponseCode.ALREADY_PARTICIPATED);
        }

        // 호스트는 직접 신청할 수 없음
        if (event.getUser().getId().equals(userId)) {
            throw new ResponseCodeException(ResponseCode.EVENT_CREATOR_CANNOT_PARTICIPATE);
        }

        // 참가 인원이 최대치에 도달했는지 확인
        if (event.getCurrentParticipants() >= event.getMaxParticipants()) {
            throw new ResponseCodeException(ResponseCode.PARTICIPANT_LIMIT_EXCEEDED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));

        Participant participant = Participant.of(event, user);
        event.addParticipant(participant);
    }

    // 이벤트 참가 취소 (권한: 소모임 멤버 또는 어드민)
    @Transactional
    public void cancelParticipation(Long userId, Long gatheringId, Long eventId) {

        checkAdminOrGatheringMember(userId, gatheringId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT));

        Participant participant = participantRepository.findByEventAndUserId(event, userId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_PARTICIPATED));

        event.removeParticipant(participant);
    }



    // 공통 권한 검증 (권한: 소모임 멤버 또는 어드민)
    private void checkAdminOrGatheringMember(Long userId, Long gatheringId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));

        boolean isAdmin = user.getUserRole().equals(UserRole.ROLE_ADMIN);
        boolean isGatheringMember = eventRepositoryCustom.isUserInGathering(gatheringId, userId);

        if (!isAdmin && !isGatheringMember) {
            throw new ResponseCodeException(ResponseCode.FORBIDDEN);
        }
    }

    // 이벤트 참가자 조회 (권한: 소모임 멤버 또는 어드민)
    @Transactional(readOnly = true)
    public List<ParticipantResponseDto> getParticipants(Long userId, Long gatheringId, Long eventId) {
        checkAdminOrGatheringMember(userId, gatheringId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT));

        List<Participant> participants = participantRepository.findAllByEvent(event);

        return participants.stream()
                .map(ParticipantResponseDto::from)
                .collect(Collectors.toList());
    }

    // 삭제 기능 (권한: 어드민, 이벤트 생성자, 소모임 생성자)
    private void checkAdminOrEventCreatorOrGatheringCreator(Long userId, Long eventId, Long gatheringId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT));

        boolean isAdmin = user.getUserRole().equals(UserRole.ROLE_ADMIN);
        boolean isEventCreator = event.getUser().getId().equals(userId);
        boolean isGatheringCreator = eventRepositoryCustom.isGatheringCreator(userId, gatheringId);

        if (!isAdmin && !isEventCreator && !isGatheringCreator) {
            throw new ResponseCodeException(ResponseCode.FORBIDDEN);
        }
    }

    // 검증 권한: 소모임 생성자 또는 이벤트 생성자
    private void checkGatheringCreatorOrEventCreator(Long userId, Long eventId, Long gatheringId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT));

        boolean isGatheringCreator = eventRepositoryCustom.isGatheringCreator(userId, gatheringId);

        boolean isEventCreator = event.getUser().getId().equals(userId);

        if (!isGatheringCreator && !isEventCreator) {
            throw new ResponseCodeException(ResponseCode.FORBIDDEN);
        }
    }
}
