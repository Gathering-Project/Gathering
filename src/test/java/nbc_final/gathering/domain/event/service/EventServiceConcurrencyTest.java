package nbc_final.gathering.domain.event.service;

import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.domain.event.dto.request.EventCreateRequestDto;
import nbc_final.gathering.domain.event.entity.Event;
import nbc_final.gathering.domain.event.repository.EventRepository;
import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.gathering.repository.GatheringRepository;
import nbc_final.gathering.domain.member.enums.MemberRole;
import nbc_final.gathering.domain.member.enums.MemberStatus;
import nbc_final.gathering.domain.user.entity.User;
import nbc_final.gathering.domain.user.enums.UserRole;
import nbc_final.gathering.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Execution(ExecutionMode.CONCURRENT)
public class EventServiceConcurrencyTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RedissonClient redissonClient;

    private Long gatheringId;
    private Long eventId;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GatheringRepository gatheringRepository;

    private static final int MAX_PARTICIPANTS = 100;

    @BeforeEach
    @Transactional
    public void setUp() {
        Gathering gathering = gatheringRepository.save(createGathering());
        gatheringId = gathering.getId();

        for (int i = 1; i <= 105; i++) {
            User user = userRepository.save(createUser((long) i));
            gathering.addMember(user, MemberRole.GUEST, MemberStatus.APPROVED);
        }

        User eventCreator = userRepository.save(createUser(1L));
        EventCreateRequestDto requestDto = EventCreateRequestDto.of(
                "테스트 이벤트", "테스트 설명", "2024-12-31", "테스트 장소", MAX_PARTICIPANTS
        );
        Event event = Event.of(requestDto.getTitle(), requestDto.getDescription(), requestDto.getDate(),
                requestDto.getLocation(), requestDto.getMaxParticipants(), gathering, eventCreator);
        eventRepository.save(event);
        eventId = event.getId();

        String participantCountKey = "event:" + eventId + ":currentParticipants";
        RAtomicLong currentParticipants = redissonClient.getAtomicLong(participantCountKey);
        currentParticipants.set(0);

        System.out.println("테스트 설정 완료: 이벤트 ID = " + eventId + ", 최대 참가자 수 = " + MAX_PARTICIPANTS);
    }

    @Test
    @DisplayName("1. Redis 참가자 수 동기화 테스트")
    public void shouldSynchronizeParticipantCountInRedis() throws InterruptedException {
        int concurrentUsers = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(concurrentUsers);
        CountDownLatch latch = new CountDownLatch(concurrentUsers);

        for (int i = 0; i < concurrentUsers; i++) {
            long testUserId = i + 2;
            executorService.submit(() -> {
                try {
                    System.out.println("사용자 " + testUserId + "가 이벤트 " + eventId + "에 참가 요청");
                    eventService.participateInEventWithDistributedLock(testUserId, gatheringId, eventId);
                    System.out.println("사용자 " + testUserId + "가 이벤트 " + eventId + "에 참가 성공");
                } catch (ResponseCodeException e) {
                    System.out.println("사용자 " + testUserId + "가 이벤트 " + eventId + "에 참가 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        String participantCountKey = "event:" + eventId + ":currentParticipants";
        RAtomicLong currentParticipants = redissonClient.getAtomicLong(participantCountKey);
        System.out.println("이벤트 " + eventId + "에 대한 최종 참가자 수 (Redis): " + currentParticipants.get());
        assertThat(currentParticipants.get()).isEqualTo(concurrentUsers);
    }

    @Test
    @DisplayName("2. 최대 참가자 수 내에서 이벤트 참가 신청 테스트")
    public void shouldAllowMaxParticipants() throws InterruptedException {
        int concurrentUsers = MAX_PARTICIPANTS;
        ExecutorService executorService = Executors.newFixedThreadPool(concurrentUsers);
        CountDownLatch latch = new CountDownLatch(concurrentUsers);

        for (int i = 0; i < concurrentUsers; i++) {
            long testUserId = i + 2;
            executorService.submit(() -> {
                try {
                    System.out.println("사용자 " + testUserId + "가 이벤트 " + eventId + "에 참가 요청");
                    eventService.participateInEventWithDistributedLock(testUserId, gatheringId, eventId);
                    System.out.println("사용자 " + testUserId + "가 이벤트 " + eventId + "에 참가 성공");
                } catch (ResponseCodeException e) {
                    System.out.println("사용자 " + testUserId + "가 이벤트 " + eventId + "에 참가 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        String participantCountKey = "event:" + eventId + ":currentParticipants";
        RAtomicLong currentParticipants = redissonClient.getAtomicLong(participantCountKey);
        System.out.println("최대 참가자 수 테스트 후 이벤트 " + eventId + "에 대한 최종 참가자 수 (Redis): " + currentParticipants.get());
        assertThat(currentParticipants.get()).isEqualTo(MAX_PARTICIPANTS);
    }

    @Test
    @DisplayName("3. 참가자 수 초과 시 참가 불가 확인")
    public void shouldRejectWhenExceedingParticipantLimit() {
        for (int i = 0; i < MAX_PARTICIPANTS + 10; i++) {
            final long testUserId = i + 2;
            if (i < MAX_PARTICIPANTS) {
                System.out.println("사용자 " + testUserId + "가 이벤트 " + eventId + "에 참가 요청");
                eventService.participateInEventWithDistributedLock(testUserId, gatheringId, eventId);
            } else {
                System.out.println("사용자 " + testUserId + "가 최대 참가자 수 초과로 이벤트 " + eventId + "에 참가 요청");
                assertThrows(ResponseCodeException.class, () -> {
                    eventService.participateInEventWithDistributedLock(testUserId, gatheringId, eventId);
                });
                System.out.println("사용자 " + testUserId + "가 최대 참가자 수 초과로 이벤트 " + eventId + "에 참가 거부됨");
            }
        }
    }

    @Test
    @DisplayName("4. 중복 참가 방지 확인")
    public void shouldPreventDuplicateParticipation() {
        long testUserId = 2;
        eventService.participateInEventWithDistributedLock(testUserId, gatheringId, eventId);
        System.out.println("사용자 " + testUserId + "가 이벤트 " + eventId + "에 첫 참가 성공");

        assertThrows(ResponseCodeException.class, () -> {
            eventService.participateInEventWithDistributedLock(testUserId, gatheringId, eventId);
        });
        System.out.println("사용자 " + testUserId + "가 중복 참가 방지로 인해 이벤트 " + eventId + "에 참가 거부됨");

        String participantCountKey = "event:" + eventId + ":currentParticipants";
        RAtomicLong currentParticipants = redissonClient.getAtomicLong(participantCountKey);
        System.out.println("중복 참가 방지 테스트 후 이벤트 " + eventId + "에 대한 최종 참가자 수 (Redis): " + currentParticipants.get());
        assertThat(currentParticipants.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("5. 참가자 수 감소 테스트")
    public void shouldDecreaseParticipantCountOnCancel() throws InterruptedException {
        long testUserId = 2;
        eventService.participateInEventWithDistributedLock(testUserId, gatheringId, eventId);
        System.out.println("사용자 " + testUserId + "가 이벤트 " + eventId + "에 참가 성공");

        String participantCountKey = "event:" + eventId + ":currentParticipants";
        RAtomicLong currentParticipants = redissonClient.getAtomicLong(participantCountKey);
        System.out.println("참가 취소 전 이벤트 " + eventId + " 참가자 수 (Redis): " + currentParticipants.get());
        assertThat(currentParticipants.get()).isEqualTo(1);

        eventService.cancelParticipation(testUserId, gatheringId, eventId);
        System.out.println("사용자 " + testUserId + "가 이벤트 " + eventId + " 참가 취소");

        System.out.println("참가 취소 후 이벤트 " + eventId + " 참가자 수 (Redis): " + currentParticipants.get());
        assertThat(currentParticipants.get()).isEqualTo(0);
    }

    @Test
    @DisplayName("6. 락 획득 실패 확인")
    public void shouldFailToAcquireLockWhenAlreadyLocked() throws InterruptedException {
        String lockKey = "event:" + eventId + ":lock";
        boolean isLockAcquiredByFirstThread = redissonClient.getLock(lockKey).tryLock(5, 10, TimeUnit.SECONDS);
        assertThat(isLockAcquiredByFirstThread).isTrue();
        System.out.println("첫 번째 스레드가 이벤트 " + eventId + "에 대해 락 획득 성공");

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        CountDownLatch latch = new CountDownLatch(1);

        executorService.submit(() -> {
            try {
                System.out.println("두 번째 스레드가 이벤트 " + eventId + "에 대해 락 획득 시도");
                boolean isLockAcquiredBySecondThread = redissonClient.getLock(lockKey).tryLock(2, 5, TimeUnit.SECONDS);
                assertThat(isLockAcquiredBySecondThread).isFalse();
                System.out.println("두 번째 스레드가 이벤트 " + eventId + "에 대해 락 획득 실패");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown();
            }
        });

        latch.await();
        executorService.shutdown();

        if (redissonClient.getLock(lockKey).isHeldByCurrentThread()) {
            redissonClient.getLock(lockKey).unlock();
            System.out.println("첫 번째 스레드가 이벤트 " + eventId + "에 대해 락 해제");
        }
    }

    private User createUser(Long userId) {
        String uniqueEmail = "testUser" + userId + "@example.com";
        String uniqueNickname = "User" + userId;
        return User.builder()
                .id(userId)
                .email(uniqueEmail)
                .password("password")
                .userRole(UserRole.ROLE_USER)
                .nickname(uniqueNickname)
                .build();
    }

    private Gathering createGathering() {
        return Gathering.of("테스트 소모임", 100, "테스트 소모임 설명입니다.");
    }
}
