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
    @Transactional(rollbackFor = {ResponseCodeException.class, CannotAcquireLockException.class, InterruptedException.class})
    public EventResponseDto createEvent(Long userId, Long gatheringId, EventCreateRequestDto requestDto) {
        verifyViewPermission(userId, gatheringId);

        Gathering gathering = getGatheringOrThrow(gatheringId);
        User user = getUserOrThrow(userId);

        Event event = createEventInstance(requestDto, gathering, user);
        eventRepository.save(event);

        resetParticipantCount(event); // Redis에 참가자 초기 카운트를 설정

        long currentParticipantsCount = getParticipantCountRedis(event.getId()); // 초기화된 카운트를 바로 가져오기

        return EventResponseDto.of(event, userId, currentParticipantsCount); // 초기화된 카운트 반영
    }

    // 이벤트 수정 (권한: 이벤트 생성자만 가능, 어드민 불가능)
    @Transactional
    public EventUpdateResponseDto updateEvent(Long userId, Long gatheringId, Long eventId, EventUpdateRequestDto requestDto) {

        verifyMembership(userId, gatheringId);
        Event event = getEventOrThrow(eventId);

        if (!isEventCreator(userId, event)) {
            throw new ResponseCodeException(ResponseCode.FORBIDDEN);
        }

        validateParticipantLimit(event, requestDto.getMaxParticipants());

        event.updateEvent(requestDto.getTitle(), requestDto.getDescription(), requestDto.getDate(),
                requestDto.getLocation(), requestDto.getMaxParticipants());

        long currentParticipantsCount = getParticipantCountRedis(eventId);

        return EventUpdateResponseDto.of(event, currentParticipantsCount);
    }


    // 이벤트 다건 조회 (권한: 소모임 멤버 또는 어드민)
    public EventListResponseDto getAllEvents(Long userId, Long gatheringId) {
        verifyViewPermission(userId, gatheringId);

        List<Event> events = eventRepository.findAllByGatheringId(gatheringId);

        // Redis에서 참가자 수를 추가하여 응답 생성
        List<EventResponseDto> eventResponseDtos = events.stream()
                .map(event -> {
                    long currentParticipantsCount = getParticipantCountRedis(event.getId());
                    return EventResponseDto.of(event, userId, currentParticipantsCount);
                })
                .collect(Collectors.toList());

        return EventListResponseDto.of(eventResponseDtos);
    }

    // 이벤트 단건 조회 (권한: 소모임 멤버 또는 어드민)
    public EventResponseDto getEvent(Long userId, Long gatheringId, Long eventId) {
        verifyViewPermission(userId, gatheringId);

        Event event = getEventOrThrow(eventId);
        long currentParticipantsCount = getParticipantCountRedis(eventId);

        List<CommentResponseDto> commentResponseDtos = getCommentsForEvent(eventId);

        return EventResponseDto.of(event, userId, commentResponseDtos, currentParticipantsCount);
    }

    // 이벤트 삭제 (권한: 이벤트 생성자 또는 어드민)
    @Transactional
    public void deleteEvent(Long userId, Long gatheringId, Long eventId) {
        verifyDeletionPermission(userId, eventId, gatheringId);

        Event event = getEventOrThrow(eventId);

        eventRepository.delete(event);
    }

    // 이벤트 참가 (분산락, 권한: 어드민 불가, 이벤트 생성자 불가, 게더링 멤버 가능)
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = {ResponseCodeException.class, CannotAcquireLockException.class, InterruptedException.class})
    public void joinEventWithLock(Long userId, Long gatheringId, Long eventId) {
        verifyMembership(userId, gatheringId);
        RLock lock = getDistributedLock(eventId);

        try {
            acquireLock(lock);

            User user = getUserOrThrow(userId);
            Event event = getEventOrThrow(eventId);

            validateParticipation(user, event, userId);

            if (isParticipantLimitExceeded(event.getMaxParticipants(), eventId)) {
                throw new ResponseCodeException(ResponseCode.PARTICIPANT_LIMIT_EXCEEDED);
            }

            registerParticipant(user, event);

        } catch (CannotAcquireLockException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseCodeException(ResponseCode.LOCK_ACQUISITION_FAILED);
        } finally {
            releaseLock(lock);
        }
    }

    // 이벤트 취소 (분산락, 권한: 어드민 불가, 이벤트 생성자 불가)
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = {ResponseCodeException.class, CannotAcquireLockException.class, InterruptedException.class})
    public void cancelParticipation(Long userId, Long gatheringId, Long eventId) {
        verifyMembership(userId, gatheringId);
        RLock lock = getDistributedLock(eventId);

        try {
            acquireLock(lock);

            User user = getUserOrThrow(userId);
            Event event = getEventOrThrow(eventId);

            checkCancelPermission(user, event);
            removeParticipant(userId, event);

        } catch (CannotAcquireLockException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseCodeException(ResponseCode.LOCK_ACQUISITION_FAILED);
        } finally {
            releaseLock(lock);
        }
    }

    // 이벤트 참가자 조회 (권한: 소모임 멤버 또는 어드민)
    @Transactional(readOnly = true)
    public List<ParticipantResponseDto> getParticipants(Long userId, Long gatheringId, Long eventId) {
        verifyViewPermission(userId, gatheringId);

        Event event = getEventOrThrow(eventId);
        List<Participant> participants = participantRepository.findAllByEvent(event);

        return participants.stream()
                .map(ParticipantResponseDto::from)
                .collect(Collectors.toList());
    }


// ------- 권한 검증 메서드 -------

    // 권한 검증: 어드민 또는 소모임 멤버인지 확인
    private void checkPermission(Long userId, Long gatheringId, boolean forAdmin, boolean forMember) {
        User user = getUserOrThrow(userId);

        boolean isAdmin = user.getUserRole().equals(UserRole.ROLE_ADMIN);
        boolean isGatheringMember = eventRepositoryCustom.isUserInGathering(gatheringId, userId);

        if ((forAdmin && isAdmin) || (forMember && isGatheringMember)) {
            return;
        }

        throw new ResponseCodeException(ResponseCode.FORBIDDEN);
    }

    // 소모임을 조회할 수 있는지 확인
    private void verifyViewPermission(Long userId, Long gatheringId) {
        checkPermission(userId, gatheringId, true, true);
    }

    // 이벤트 삭제 권한 검증: 어드민이 아니고 이벤트 생성자가 아닌 경우
    private void verifyDeletionPermission(Long userId, Long eventId, Long gatheringId) {

        boolean isGatheringMember = eventRepositoryCustom.isUserInGathering(gatheringId, userId);
        if (!isGatheringMember) {
            throw new ResponseCodeException(ResponseCode.FORBIDDEN);
        }

        Event event = getEventOrThrow(eventId);
        User user = getUserOrThrow(userId);

        boolean isAdmin = user.getUserRole().equals(UserRole.ROLE_ADMIN);
        boolean isEventCreator = event.getUser().getId().equals(userId);

        if (!isAdmin && !isEventCreator) {
            throw new ResponseCodeException(ResponseCode.FORBIDDEN);
        }
    }

    // 소모임 멤버 여부 확인
    private void verifyMembership(Long userId, Long gatheringId) {
        User user = getUserOrThrow(userId);
        boolean isGatheringMember = eventRepositoryCustom.isUserInGathering(gatheringId, userId);

        if (!isGatheringMember) {
            throw new ResponseCodeException(ResponseCode.FORBIDDEN);
        }
    }

    // 이벤트 생성자인지 확인
    private boolean isEventCreator(Long userId, Event event) {
        return event.getUser().getId().equals(userId);
    }

// ------- 락 관리 메서드 -------

    // Redis 락 관리 메서드
    private RLock getDistributedLock(Long eventId) {
        return redissonClient.getLock("event:" + eventId + ":lock");
    }

    // 분산 락 획득
    private void acquireLock(RLock lock) throws InterruptedException {
        if (!lock.tryLock(3, 5, TimeUnit.SECONDS)) {
            throw new ResponseCodeException(ResponseCode.LOCK_ACQUISITION_FAILED);
        }
    }

    // 락 해제
    private void releaseLock(RLock lock) {
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

// ------- 유틸리티 메서드 -------

    // 이벤트 확인
    private Event getEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT));
    }

    // 현재 참가자 수 조회
    public long getParticipantCountRedis(Long eventId) {
        return redissonClient.getAtomicLong("event:" + eventId + ":currentParticipants").get();
    }

    // 소모임 조회
    private Gathering getGatheringOrThrow(Long gatheringId) {
        return gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GATHERING));
    }

    // 사용자 조회
    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));
    }

// ------- 생성 관련 메서드 -------

    // 이벤트 생성 인스턴스 생성
    private Event createEventInstance(EventCreateRequestDto requestDto, Gathering gathering, User user) {
        return Event.of(requestDto.getTitle(), requestDto.getDescription(), requestDto.getDate(),
                requestDto.getLocation(), requestDto.getMaxParticipants(), gathering, user);
    }

    // Redis에 참가자 수 초기화
    private void resetParticipantCount(Event event) {
        String participantCountKey = "event:" + event.getId() + ":currentParticipants";
        RAtomicLong currentParticipants = redissonClient.getAtomicLong(participantCountKey);
        currentParticipants.set(1);
    }

// ------- 참여 관련 메서드 -------

    // 참가자 수 제한 검증
    private void validateParticipantLimit(Event event, int maxParticipants) {
        if (event.getCurrentParticipants() > maxParticipants) {
            throw new ResponseCodeException(ResponseCode.INVALID_MAX_PARTICIPANTS);
        }
    }

    // 이벤트 참가 조건 검증
    private void validateParticipation(User user, Event event, Long userId) {
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

    // 참가자 수가 제한을 초과했는지 확인
    private boolean isParticipantLimitExceeded(int maxParticipants, Long eventId) {
        long currentCount = getParticipantCountRedis(eventId);
        return currentCount >= maxParticipants;
    }

    // 참가자 등록
    private void registerParticipant(User user, Event event) {
        Participant participant = Participant.of(event, user);
        participantRepository.save(participant);

        long updatedCount = addParticipant(event.getId());
        if (updatedCount > event.getMaxParticipants()) {
            undoRegistration(participant, updatedCount);
        }
    }

    // 참가자 등록 취소 및 롤백
    private void undoRegistration(Participant participant, long updatedCount) {
        removeParticipant(participant.getEvent().getId());
        participantRepository.delete(participant);
        throw new ResponseCodeException(ResponseCode.PARTICIPANT_LIMIT_EXCEEDED);
    }

    // Redis에서 참가자 수 증가
    private long addParticipant(Long eventId) {
        RAtomicLong currentParticipants = redissonClient.getAtomicLong("event:" + eventId + ":currentParticipants");
        return currentParticipants.incrementAndGet();
    }

    // Redis에서 참가자 수 감소
    private void removeParticipant(Long eventId) {
        RAtomicLong currentParticipants = redissonClient.getAtomicLong("event:" + eventId + ":currentParticipants");
        if (currentParticipants.get() > 0) {
            currentParticipants.decrementAndGet();
        }
    }

// ------- 취소 관련 메서드 -------

    // 취소 관련 조건 검증
    private void checkCancelPermission(User user, Event event) {
        if (user.getUserRole().equals(UserRole.ROLE_ADMIN)) {
            throw new ResponseCodeException(ResponseCode.ADMIN_CANNOT_CANCEL_PARTICIPATION);
        }
        if (event.getUser().getId().equals(user.getId())) {
            throw new ResponseCodeException(ResponseCode.EVENT_CREATOR_CANNOT_CANCEL);
        }
    }

    // 참가자 삭제
    private void removeParticipant(Long userId, Event event) {
        Participant participant = participantRepository.findByEventAndUserId(event, userId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_PARTICIPATED));

        event.removeParticipant(participant);
        participantRepository.delete(participant);
        removeParticipant(event.getId());
    }

// ------- 조회 관련 메서드 -------

    // 이벤트에 대한 댓글 조회
    private List<CommentResponseDto> getCommentsForEvent(Long eventId) {
        return commentRepository.findByEventId(eventId).stream()
                .map(CommentResponseDto::of)
                .collect(Collectors.toList());
    }

}
