package nbc_final.gathering.domain.event.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
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

    @PersistenceContext
    private EntityManager entityManager;

    private static final int MAX_PARTICIPANTS = 100;

    private List<Long> userIds = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        // 트랜잭션을 수동으로 관리하여 데이터가 즉시 커밋되도록 함
        Gathering gathering = createGathering();
        gatheringRepository.saveAndFlush(gathering);
        gatheringId = gathering.getId();

        // 사용자 생성 및 소모임 멤버 추가
        for (int i = 1; i <= MAX_PARTICIPANTS + 10; i++) {
            User user = createUser();
            userRepository.saveAndFlush(user);
            gathering.addMember(user, MemberRole.GUEST, MemberStatus.APPROVED);
            userIds.add(user.getId());
        }

        gatheringRepository.saveAndFlush(gathering);

        // 이벤트 생성
        User eventCreator = createUser();
        userRepository.saveAndFlush(eventCreator);
        EventCreateRequestDto requestDto = EventCreateRequestDto.of(
                "테스트 이벤트", "테스트 설명", "2024-12-31", "테스트 장소", MAX_PARTICIPANTS
        );
        Event event = Event.of(requestDto.getTitle(), requestDto.getDescription(), requestDto.getDate(),
                requestDto.getLocation(), requestDto.getMaxParticipants(), gathering, eventCreator);
        eventRepository.saveAndFlush(event);
        eventId = event.getId();

        // Redis 참가자 수 초기화
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
            long testUserId = userIds.get(i);
            executorService.submit(() -> {
                try {
                    System.out.println("사용자 " + testUserId + "가 이벤트 " + eventId + "에 참가 요청");
                    eventService.joinEventWithLock(testUserId, eventId);
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
        ExecutorService executorService = Executors.newFixedThreadPool(20); // 스레드 수를 적절히 설정
        CountDownLatch latch = new CountDownLatch(concurrentUsers);

        for (int i = 0; i < concurrentUsers; i++) {
            final int index = i;
            executorService.submit(() -> {
                long testUserId = userIds.get(index);
                try {
                    System.out.println("사용자 " + testUserId + "가 이벤트 " + eventId + "에 참가 요청");
                    eventService.cancelParticipation(testUserId, eventId);
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
            final int index = i;
            long testUserId = userIds.get(index);
            if (i < MAX_PARTICIPANTS) {
                System.out.println("사용자 " + testUserId + "가 이벤트 " + eventId + "에 참가 요청");
                eventService.cancelParticipation(testUserId, eventId);
            } else {
                System.out.println("사용자 " + testUserId + "가 최대 참가자 수 초과로 이벤트 " + eventId + "에 참가 요청");
                assertThrows(ResponseCodeException.class, () -> {
                    eventService.cancelParticipation(testUserId, eventId);
                });
                System.out.println("사용자 " + testUserId + "가 최대 참가자 수 초과로 이벤트 " + eventId + "에 참가 거부됨");
            }
        }
    }

    @Test
    @DisplayName("4. 중복 참가 방지 확인")
    public void shouldPreventDuplicateParticipation() {
        long testUserId = userIds.get(0);
        eventService.cancelParticipation(testUserId, eventId);
        System.out.println("사용자 " + testUserId + "가 이벤트 " + eventId + "에 첫 참가 성공");

        assertThrows(ResponseCodeException.class, () -> {
            eventService.cancelParticipation(testUserId, eventId);
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
        long testUserId = userIds.get(0);
        eventService.cancelParticipation(testUserId, eventId);
        System.out.println("사용자 " + testUserId + "가 이벤트 " + eventId + "에 참가 성공");

        String participantCountKey = "event:" + eventId + ":currentParticipants";
        RAtomicLong currentParticipants = redissonClient.getAtomicLong(participantCountKey);
        System.out.println("참가 취소 전 이벤트 " + eventId + " 참가자 수 (Redis): " + currentParticipants.get());
        assertThat(currentParticipants.get()).isEqualTo(1);

        eventService.cancelParticipation(testUserId, eventId);
        System.out.println("사용자 " + testUserId + "가 이벤트 " + eventId + " 참가 취소");

        System.out.println("참가 취소 후 이벤트 " + eventId + " 참가자 수 (Redis): " + currentParticipants.get());
        assertThat(currentParticipants.get()).isEqualTo(0);
    }

    @Test
    @DisplayName("6. 락 획득 실패 확인")
    public void shouldFailToAcquireLockWhenAlreadyLocked() throws InterruptedException {
        String lockKey = "event:" + eventId + ":lock";
        RLock lock = redissonClient.getLock(lockKey);

        // 첫 번째 스레드가 락을 획득
        boolean isLockAcquiredByFirstThread = lock.tryLock(5, 10, TimeUnit.SECONDS);
        assertThat(isLockAcquiredByFirstThread).isTrue();
        System.out.println("첫 번째 스레드가 이벤트 " + eventId + "에 대해 락 획득 성공");

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        CountDownLatch latch = new CountDownLatch(1);

        // 두 번째 스레드가 동일한 락을 획득하려고 시도
        executorService.submit(() -> {
            try {
                System.out.println("두 번째 스레드가 이벤트 " + eventId + "에 대해 락 획득 시도");
                boolean isLockAcquiredBySecondThread = lock.tryLock(2, 5, TimeUnit.SECONDS);
                assertThat(isLockAcquiredBySecondThread).isFalse(); // 두 번째 스레드는 락을 얻지 못해야 함
                System.out.println("두 번째 스레드가 이벤트 " + eventId + "에 대해 락 획득 실패");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown();
            }
        });

        latch.await();
        executorService.shutdown();

        // 첫 번째 스레드가 락을 해제
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            System.out.println("첫 번째 스레드가 이벤트 " + eventId + "에 대해 락 해제");
        }
    }

    private User createUser() {
        String uniqueEmail = "testUser" + System.nanoTime() + "@example.com";
        String uniqueNickname = "User" + System.nanoTime();
        return User.builder()
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
