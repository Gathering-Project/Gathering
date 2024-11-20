package nbc_final.gathering.domain.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc_final.gathering.common.annotation.DistributedLock;
import nbc_final.gathering.common.elasticsearch.EventElasticSearchRepository;
import nbc_final.gathering.common.alarmconfig.AlarmDto;
import nbc_final.gathering.common.alarmconfig.AlarmService;
import nbc_final.gathering.common.exception.ResponseCode;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.common.kafka.util.KafkaNotificationUtil;
import nbc_final.gathering.domain.comment.dto.response.CommentResponseDto;
import nbc_final.gathering.domain.comment.repository.CommentRepository;
import nbc_final.gathering.domain.event.dto.EventElasticDto;
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
import nbc_final.gathering.domain.member.entity.Member;
import nbc_final.gathering.domain.member.enums.MemberStatus;
import nbc_final.gathering.domain.member.repository.MemberRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EventService {

    private final ParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EventRepositoryCustom eventRepositoryCustom;
    private final GatheringRepository gatheringRepository;
    private final CommentRepository commentRepository;
    private final AlarmService alarmService;
    private final MemberRepository memberRepository;
    private final RedissonClient redissonClient;
    private final EventElasticSearchRepository eventElasticSearchRepository;


    //이벤트 검색(형태소 기반)
    public List<EventResponseDto> searchEvents(String keyword) {
        List<EventElasticDto> searchResults = eventElasticSearchRepository.findByTitleContainingOrDescriptionContaining(keyword, keyword);
        return searchResults.stream()
                .map(EventResponseDto::of)
                .collect(Collectors.toList());
    }


    // 이벤트 생성 (권한: 소모임 멤버 또는 어드민)
    @Transactional(rollbackFor = {ResponseCodeException.class, CannotAcquireLockException.class, InterruptedException.class})
    public EventResponseDto createEvent(Long userId, Long gatheringId, EventCreateRequestDto requestDto) {
        verifyViewPermission(userId, gatheringId);

        Gathering gathering = getGatheringOrThrow(gatheringId);
        User user = getUserOrThrow(userId);
        List<Member> members = memberRepository.findAllByGatheringId(gatheringId);


        Event event = createEventInstance(requestDto, gathering, user);
        eventRepository.save(event);

        //엘라스틱 서치
        EventElasticDto eventElasticDto = EventElasticDto.of(event);
        eventElasticSearchRepository.save(eventElasticDto); //엘라스틱 서치 추가

        resetParticipantCount(event); // Redis에 참가자 초기 카운트를 설정

        Participant creatorParticipant = Participant.of(event, user);
        participantRepository.save(creatorParticipant);

        long currentParticipantsCount = getParticipantCountRedis(event.getId()); // 초기화된 카운트를 바로 가져오기

        // APPROVED 상태의 멤버만 필터링
        List<Member> approvedMembers = members.stream()
                .filter(member -> member.getStatus() == MemberStatus.APPROVED)
                .collect(Collectors.toList());

        // 이벤트 생성 알림 메시지
        String message = "이벤트 '" + event.getTitle() + "'이(가) 생성되었습니다.";

        // 승인된 멤버에게 알림 전송
        approvedMembers.forEach(member -> {
            AlarmDto.AlarmMessageReq alarmMessageReq = new AlarmDto.AlarmMessageReq(
                    member.getUser().getId(), message
            );
            alarmService.sendAlarm(alarmMessageReq);  // AlarmService를 사용하여 알림 전송
            log.info("알림 전송: 멤버 ID={}, 메시지={}", member.getUser().getId(), message);
        });

        return EventResponseDto.of(event, userId, currentParticipantsCount); // 초기화된 카운트 반영
    }

    // 이벤트 수정 (권한: 이벤트 생성자만 가능, 어드민 본인 이벤트만 가능)
    @Transactional
    public EventUpdateResponseDto updateEvent(Long userId, Long gatheringId, Long eventId, EventUpdateRequestDto requestDto) {

        User user = getUserOrThrow(userId);

        if (!user.getUserRole().equals(UserRole.ROLE_ADMIN)) {
            verifyMembership(userId, gatheringId);
        }

        Event event = getEventOrThrow(eventId);

        if (!(isEventCreator(userId, event) || (user.getUserRole().equals(UserRole.ROLE_ADMIN) && isEventCreator(userId, event)))) {
            throw new ResponseCodeException(ResponseCode.FORBIDDEN);
        }

        long currentParticipantsCount = getParticipantCountRedis(eventId);

        if (requestDto.getMaxParticipants() < currentParticipantsCount) {
            throw new ResponseCodeException(ResponseCode.INVALID_MAX_PARTICIPANTS);
        }

        validateParticipantLimit(event, requestDto.getMaxParticipants());

        event.updateEvent(requestDto.getTitle(), requestDto.getDescription(), requestDto.getDate(),
                requestDto.getLocation(), requestDto.getMaxParticipants());

        List<Participant> participants = participantRepository.findAllByEvent(event);

        // 각 참가자에게 이벤트 수정 알림 전송
        participants.forEach(participant -> {
            // 알림 메시지 생성
            String message = "이벤트 '" + event.getTitle() + "'이(가) 수정되었습니다.";

            // 알림을 AlarmService를 통해 전송
            AlarmDto.AlarmMessageReq alarmMessageReq = new AlarmDto.AlarmMessageReq(
                    participant.getUser().getId(), message
            );
            alarmService.sendAlarm(alarmMessageReq);  // AlarmService를 사용하여 알림 전송
            log.info("알림 전송: 멤버 ID={}, 메시지={}", participant.getUser().getId(), message);
        });

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
        User user = getUserOrThrow(userId);

        if (!user.getUserRole().equals(UserRole.ROLE_ADMIN)) {
            verifyDeletionPermission(userId, eventId, gatheringId);
        }

        Event event = getEventOrThrow(eventId);

        // 이벤트 참가자 조회
        List<Participant> participants = participantRepository.findAllByEvent(event);

        // 각 참가자에게 이벤트 삭제 알림 전송
        participants.forEach(participant -> {
            // 알림 메시지 생성
            String message = "이벤트 '" + event.getTitle() + "'이(가) 삭제되었습니다.";

            // 알림을 AlarmService를 통해 전송
            AlarmDto.AlarmMessageReq alarmMessageReq = new AlarmDto.AlarmMessageReq(
                    participant.getUser().getId(), message
            );
            alarmService.sendAlarm(alarmMessageReq);  // AlarmService를 사용하여 알림 전송
            log.info("알림 전송: 멤버 ID={}, 메시지={}", participant.getUser().getId(), message);
        });

        eventRepository.delete(event);
    }

    // 이벤트 참가 (분산락, 권한: 어드민 불가, 이벤트 생성자 불가, 게더링 멤버 가능)
    @DistributedLock(key = "event:{#eventId}:lock", waitTime = 10, leaseTime = 15)
    @Transactional(rollbackFor = ResponseCodeException.class)
    public void joinEventWithLock(Long userId, Long eventId) {
        User user = getUserOrThrow(userId);
        Event event = getEventOrThrow(eventId);

            validateParticipation(user, event, userId);

        if (isParticipantLimitExceeded(event.getMaxParticipants(), eventId)) {
            throw new ResponseCodeException(ResponseCode.PARTICIPANT_LIMIT_EXCEEDED);
        }

        registerParticipant(user, event);

        // 참가자에게 알림 전송
        String message = "이벤트 참가 신청이 완료되었습니다.";
        AlarmDto.AlarmMessageReq alarmMessageReq = new AlarmDto.AlarmMessageReq(userId, message);
    }

    // 이벤트 취소 (분산락, 권한: 어드민 불가, 이벤트 생성자 불가)
    @DistributedLock(key = "event:{#eventId}:lock", waitTime = 10, leaseTime = 15)
    @Transactional(rollbackFor = {ResponseCodeException.class})
    public void cancelParticipation(Long userId, Long eventId) {
        User user = getUserOrThrow(userId);
        Event event = getEventOrThrow(eventId);

        // 이벤트 참가 여부 확인
        Participant participant = participantRepository.findByEventAndUserId(event, userId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_PARTICIPATED));

        // 취소 권한 확인
        checkCancelPermission(user, event);

        // 참가자 제거
        removeParticipant(participant, event);

        // 참가 취소 알림 전송
        String message = "이벤트 참가가 취소되었습니다.";
        AlarmDto.AlarmMessageReq alarmMessageReq = new AlarmDto.AlarmMessageReq(userId, message);

        // AlarmService를 사용하여 알림 전송
        alarmService.sendAlarm(alarmMessageReq);
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
        boolean isEventCreator = event.getUser().getId().equals(userId);

        if (!isEventCreator) {
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
        eventRepository.save(event);
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
    private void removeParticipant(Participant participant, Event event) {
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
