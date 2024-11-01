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
import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import nbc_final.gathering.domain.comment.entity.Comment;

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
    private final RedissonClient redissonClient;

    // 이벤트 생성 (권한: 소모임 멤버 또는 어드민)
//    @Transactional
//    public EventResponseDto createEvent(Long userId, Long gatheringId, EventCreateRequestDto requestDto) {
//        checkAdminOrGatheringMemberForCreation(userId, gatheringId);
//
//        Gathering gathering = getGatheringOrThrow(gatheringId);
//        User user = getUserOrThrow(userId);
//
//        Event event = Event.of(requestDto.getTitle(), requestDto.getDescription(), requestDto.getDate(),
//                requestDto.getLocation(), requestDto.getMaxParticipants(), gathering, user);
//        eventRepository.save(event);
//
//        // 이벤트 생성자는 자동으로 참가
//        Participant participant = Participant.of(event, user);
//        event.addParticipant(participant);
//
//        return EventResponseDto.of(event, userId);
//    }
    @Transactional(rollbackFor = {ResponseCodeException.class, CannotAcquireLockException.class, InterruptedException.class})
    public EventResponseDto createEvent(Long userId, Long gatheringId, EventCreateRequestDto requestDto) {
        checkAdminOrGatheringMemberForCreation(userId, gatheringId);

        Gathering gathering = getGatheringOrThrow(gatheringId);
        User user = getUserOrThrow(userId);

        Event event = Event.of(requestDto.getTitle(), requestDto.getDescription(), requestDto.getDate(),
                requestDto.getLocation(), requestDto.getMaxParticipants(), gathering, user);
        eventRepository.save(event);

        Participant participant = Participant.of(event, user);
        event.addParticipant(participant);

        String participantCountKey = "event:" + event.getId() + ":currentParticipants";
        RAtomicLong currentParticipants = redissonClient.getAtomicLong(participantCountKey);
        currentParticipants.set(1);  // 초기 참가자 수를 1로 설정

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
//    public EventListResponseDto getAllEvents(Long userId, Long gatheringId) {
//        checkAdminOrGatheringMemberForView(userId, gatheringId);
//        List<Event> events = eventRepository.findAllByGatheringId(gatheringId);
//        return EventListResponseDto.of(events, userId);
//    }

    public EventListResponseDto getAllEvents(Long userId, Long gatheringId) {
        checkAdminOrGatheringMemberForView(userId, gatheringId);

        List<Event> events = eventRepository.findAllByGatheringId(gatheringId);

        // 각 이벤트에 대해 Redis의 참가자 수를 추가하여 응답 생성
        List<EventResponseDto> eventResponseDtos = events.stream()
                .map(event -> {
                    long currentParticipantsCount = getCurrentParticipantsFromRedis(event.getId());
                    return EventResponseDto.of(event, userId, currentParticipantsCount);
                })
                .collect(Collectors.toList());

        return EventListResponseDto.of(eventResponseDtos);
    }

    // 이벤트 단건 조회 (권한: 소모임 멤버 또는 어드민)
//    public EventResponseDto getEvent(Long userId, Long gatheringId, Long eventId) {
//        checkAdminOrGatheringMemberForView(userId, gatheringId);
//
//        Event event = getEventOrThrow(eventId);
//
//        List<Comment> comments = commentRepository.findByEventId(eventId);
//        List<CommentResponseDto> commentResponseDtos = comments.stream()
//                .map(CommentResponseDto::of)
//                .collect(Collectors.toList());
//
//        return EventResponseDto.of(event, userId, commentResponseDtos);
//    }
    public EventResponseDto getEvent(Long userId, Long gatheringId, Long eventId) {
        checkAdminOrGatheringMemberForView(userId, gatheringId);

        Event event = getEventOrThrow(eventId);

        // Redis에서 참가자 수를 가져옴
        long currentParticipantsCount = getCurrentParticipantsFromRedis(eventId);

        List<Comment> comments = commentRepository.findByEventId(eventId);
        List<CommentResponseDto> commentResponseDtos = comments.stream()
                .map(CommentResponseDto::of)
                .collect(Collectors.toList());

        return EventResponseDto.of(event, userId, commentResponseDtos, currentParticipantsCount);
    }


    // 이벤트 삭제 (권한: 이벤트 생성자 또는 어드민)
    @Transactional
    public void deleteEvent(Long userId, Long gatheringId, Long eventId) {
        checkAdminOrEventCreatorForDeletion(userId, eventId, gatheringId);

        Event event = getEventOrThrow(eventId);

        eventRepository.delete(event);
    }

//    // 이벤트 참가 (권한: 어드민 불가, 이벤트 생성자 불가, 게더링 멤버 가능) 분산락 X
//    @Transactional
//    public void participateInEvent(Long userId, Long gatheringId, Long eventId) {
//        User user = getUserOrThrow(userId);
//        Event event = getEventOrThrow(eventId);
//
//        validateParticipantConditions(user, event, userId);
//
//        if (event.getCurrentParticipants() >= event.getMaxParticipants()) {
//            throw new ResponseCodeException(ResponseCode.PARTICIPANT_LIMIT_EXCEEDED);
//        }
//
//        checkAdminOrGatheringMemberForCreation(userId, gatheringId);
//
//        Participant participant = Participant.of(event, user);
//        event.addParticipant(participant);
//    }

    // 락을 사용한 이벤트 참가
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = {ResponseCodeException.class, CannotAcquireLockException.class, InterruptedException.class})
    public void participateInEventWithDistributedLock(Long userId, Long gatheringId, Long eventId) {
        String lockKey = "event:" + eventId + ":lock"; // 이벤트별 고유 락 키 생성
        RLock lock = redissonClient.getLock(lockKey);
        String participantCountKey = "event:" + eventId + ":currentParticipants"; // 참가자 수를 관리할 Redis 키
        RAtomicLong currentParticipants = redissonClient.getAtomicLong(participantCountKey);

        try {
            // 락을 시도하고 지정된 시간 내에 획득하지 못하면 예외 발생
            if (!lock.tryLock(3, 5, TimeUnit.SECONDS)) {
                throw new ResponseCodeException(ResponseCode.LOCK_ACQUISITION_FAILED);
            }

            // 사용자와 이벤트 정보를 가져옴
            User user = getUserOrThrow(userId);
            Event event = getEventOrThrow(eventId);

            if (user.getUserRole().equals(UserRole.ROLE_ADMIN)) {
                throw new ResponseCodeException(ResponseCode.ADMIN_CANNOT_PARTICIPATE);
            }
            if (event.getUser().getId().equals(userId)) {
                throw new ResponseCodeException(ResponseCode.EVENT_CREATOR_CANNOT_PARTICIPATE);
            }

            // 현재 참가자 수 확인 및 참가 제한 검사
            long currentCount = currentParticipants.get();
            if (currentCount >= event.getMaxParticipants()) {
                throw new ResponseCodeException(ResponseCode.PARTICIPANT_LIMIT_EXCEEDED);
            }

            // 중복 참가 여부 확인
            participantRepository.findByEventAndUserId(event, userId)
                    .ifPresent(participant -> {
                        throw new ResponseCodeException(ResponseCode.ALREADY_PARTICIPATED);
                    });

            // 참가자를 DB에 저장
            Participant participant = Participant.of(event, user);
            participantRepository.save(participant);

            // Redis 참가자 수 증가 및 확인
            long updatedCount = currentParticipants.incrementAndGet();
            if (updatedCount > event.getMaxParticipants()) {
                // 만약 이 시점에서 참가자 수가 제한을 초과하면 참가를 취소하고 예외를 발생시킴
                currentParticipants.decrementAndGet(); // Redis 참가자 수 롤백
                participantRepository.delete(participant); // DB에서 참가자 삭제
                throw new ResponseCodeException(ResponseCode.PARTICIPANT_LIMIT_EXCEEDED);
            }

            System.out.println("Updated Redis participant count after participation: " + updatedCount);

        } catch (CannotAcquireLockException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseCodeException(ResponseCode.LOCK_ACQUISITION_FAILED);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }


//    // 이벤트 참가 취소 (권한: 어드민 불가, 이벤트 생성자 불가)
//    @Transactional
//    public void cancelParticipation(Long userId, Long gatheringId, Long eventId) {
//        User user = getUserOrThrow(userId);
//
//        if (user.getUserRole().equals(UserRole.ROLE_ADMIN)) {
//            throw new ResponseCodeException(ResponseCode.ADMIN_CANNOT_CANCEL_PARTICIPATION);
//        }
//
//        Event event = getEventOrThrow(eventId);
//
//        if (event.getUser().getId().equals(userId)) {
//            throw new ResponseCodeException(ResponseCode.EVENT_CREATOR_CANNOT_CANCEL);
//        }
//
//        Participant participant = participantRepository.findByEventAndUserId(event, userId)
//                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_PARTICIPATED));
//
//        event.removeParticipant(participant);
//    }

    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = {ResponseCodeException.class, CannotAcquireLockException.class, InterruptedException.class})
    public void cancelParticipation(Long userId, Long gatheringId, Long eventId) {
        String lockKey = "event:" + eventId + ":lock"; // 이벤트별 고유 락 키 생성
        RLock lock = redissonClient.getLock(lockKey);
        String participantCountKey = "event:" + eventId + ":currentParticipants"; // 참가자 수를 관리할 Redis 키
        RAtomicLong currentParticipants = redissonClient.getAtomicLong(participantCountKey);

        try {
            // 락을 시도하고 지정된 시간 내에 획득하지 못하면 예외 발생
            if (!lock.tryLock(3, 5, TimeUnit.SECONDS)) {
                throw new ResponseCodeException(ResponseCode.LOCK_ACQUISITION_FAILED);
            }

            // 사용자와 이벤트 정보를 가져옴
            User user = getUserOrThrow(userId);
            Event event = getEventOrThrow(eventId);

            // 관리자와 이벤트 생성자의 참가 취소 요청에 대한 예외 처리
            if (user.getUserRole().equals(UserRole.ROLE_ADMIN)) {
                throw new ResponseCodeException(ResponseCode.ADMIN_CANNOT_CANCEL_PARTICIPATION);
            }
            if (event.getUser().getId().equals(userId)) {
                throw new ResponseCodeException(ResponseCode.EVENT_CREATOR_CANNOT_CANCEL);
            }

            // 참가 여부 확인 및 참가자 객체 가져오기
            Participant participant = participantRepository.findByEventAndUserId(event, userId)
                    .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_PARTICIPATED));

            // 참가자를 DB에서 제거
            event.removeParticipant(participant);
            participantRepository.delete(participant);

            // Redis 참가자 수 감소
            if (currentParticipants.get() > 0) {
                currentParticipants.decrementAndGet();
            }

            System.out.println("Updated Redis participant count after cancellation: " + currentParticipants.get());

        } catch (CannotAcquireLockException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseCodeException(ResponseCode.LOCK_ACQUISITION_FAILED);
        } finally {
            // 락을 획득한 스레드만 해제 가능
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
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


    public long getCurrentParticipantsFromRedis(Long eventId) {
        String participantCountKey = "event:" + eventId + ":currentParticipants";
        RAtomicLong currentParticipants = redissonClient.getAtomicLong(participantCountKey);
        return currentParticipants.get();
    }
}
