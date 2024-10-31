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
import org.redisson.api.RLock;
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

    private Long userId;
    private Long gatheringId;
    private Long eventId;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GatheringRepository gatheringRepository;

    @BeforeEach
    @Transactional
    public void setUp() {
        System.out.println("테스트 데이터 준비: 사용자와 소모임 생성");

        Gathering gathering = gatheringRepository.save(getGatheringOrThrow(1L));
        gatheringId = gathering.getId();
        System.out.println("소모임 " + gatheringId + "이 생성되었습니다.");

        for (int i = 1; i <= 105; i++) {
            User user = userRepository.save(getUserOrThrow((long) i));
            gathering.addMember(user, MemberRole.GUEST, MemberStatus.APPROVED);
        }
        gatheringRepository.save(gathering);
        System.out.println("총 105명의 사용자가 생성되고 게더링 멤버로 추가되었습니다.");

        User eventCreator = getUserOrThrow(userId);
        userRepository.save(eventCreator);

        EventCreateRequestDto requestDto = EventCreateRequestDto.of(
                "Test Event",
                "Test Description",
                "2024-12-31",
                "Test Location",
                100
        );

        Event event = Event.of(requestDto.getTitle(), requestDto.getDescription(), requestDto.getDate(),
                requestDto.getLocation(), requestDto.getMaxParticipants(), gathering, eventCreator);
        eventRepository.save(event);
        eventId = event.getId();
        System.out.println("이벤트 " + eventId + "가 생성되었습니다.");
    }

    @Test
    @DisplayName("1. 최대 참가자 수 내에서 이벤트 참가 신청 테스트")
    public void shouldAllowMaxParticipants() throws InterruptedException {
        System.out.println("테스트 시작: 최대 참가자 수 내에서 이벤트 참가 신청");

        int concurrentUsers = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(concurrentUsers);
        CountDownLatch latch = new CountDownLatch(concurrentUsers);

        for (int i = 0; i < concurrentUsers; i++) {
            long testUserId = i + 2;
            executorService.submit(() -> {
                try {
                    eventService.participateInEventWithDistributedLock(testUserId, gatheringId, eventId);
                    System.out.println("사용자 " + testUserId + "이(가) 이벤트 참가에 성공했습니다.");
                } catch (ResponseCodeException e) {
                    System.out.println("사용자 " + testUserId + "이(가) 이벤트 참가에 실패했습니다: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        Event event = eventRepository.findById(eventId).orElseThrow();
        System.out.println("최종 참가자 수: " + event.getCurrentParticipants());
        assertThat(event.getCurrentParticipants()).isLessThanOrEqualTo(event.getMaxParticipants());
        System.out.println("테스트 종료: 최대 참가자 수 내에서 이벤트 참가 신청 테스트");
    }

    @Test
    @DisplayName("2. 동시 참가 시도 시 분산 락을 적용하여 인원 제한 확인")
    public void shouldLimitConcurrentParticipantsWithLock() throws InterruptedException {
        System.out.println("테스트 시작: 동시 참가 시도 시 분산 락을 적용하여 인원 제한 확인");

        int concurrentUsers = 105;
        ExecutorService executorService = Executors.newFixedThreadPool(concurrentUsers);
        CountDownLatch latch = new CountDownLatch(concurrentUsers);

        for (int i = 0; i < concurrentUsers; i++) {
            long testUserId = i + 2;
            executorService.submit(() -> {
                try {
                    eventService.participateInEventWithDistributedLock(testUserId, gatheringId, eventId);
                    System.out.println("사용자 " + testUserId + "이(가) 이벤트 참가에 성공했습니다.");
                } catch (ResponseCodeException e) {
                    System.out.println("사용자 " + testUserId + "이(가) 이벤트 참가에 실패했습니다: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(3, TimeUnit.SECONDS);
        executorService.shutdown();

        Event event = eventRepository.findById(eventId).orElseThrow();
        System.out.println("최종 참가자 수: " + event.getCurrentParticipants());
        assertThat(event.getCurrentParticipants()).isEqualTo(100);
        System.out.println("테스트 종료: 동시 참가 시도 시 분산 락을 적용하여 인원 제한 확인");
    }

    @Test
    @DisplayName("3. 인원 초과 시 참가 불가 확인")
    public void shouldRejectWhenExceedingParticipantLimit() {
        System.out.println("테스트 시작: 인원 초과 시 참가 불가 확인");

        for (int i = 0; i < 120; i++) {
            final long testUserId = i + 2;
            if (i < 100) {
                eventService.participateInEventWithDistributedLock(testUserId, gatheringId, eventId);
                System.out.println("사용자 " + testUserId + "이(가) 이벤트 참가에 성공했습니다.");
            } else {
                assertThrows(ResponseCodeException.class, () -> {
                    eventService.participateInEventWithDistributedLock(testUserId, gatheringId, eventId);
                });
                System.out.println("사용자 " + testUserId + "이(가) 인원 초과로 이벤트 참가에 실패해야 합니다.");
            }
        }
        System.out.println("테스트 종료: 인원 초과 시 참가 불가 확인");
    }

    @Test
    @DisplayName("4. 중복 참가 신청 차단 확인")
    public void shouldRejectDuplicateParticipation() {
        System.out.println("테스트 시작: 중복 참가 신청 차단 확인");

        long testUserId = 2; // 동일 사용자 ID 사용
        for (int i = 0; i < 30; i++) {
            if (i == 0) {
                eventService.participateInEventWithDistributedLock(testUserId, gatheringId, eventId);
                System.out.println("사용자 " + testUserId + "이(가) 이벤트 참가에 성공했습니다.");
            } else {
                assertThrows(ResponseCodeException.class, () -> {
                    eventService.participateInEventWithDistributedLock(testUserId, gatheringId, eventId);
                });
                System.out.println("사용자 " + testUserId + "이(가) 중복 신청으로 이벤트 참가에 실패해야 합니다.");
            }
        }
        System.out.println("테스트 종료: 중복 참가 신청 차단 확인");
    }

    @Test
    @DisplayName("5. 락 획득 성공 확인")
    public void shouldAcquireLockSuccessfully() throws InterruptedException {
        System.out.println("테스트 시작: 락 획득 성공 확인");

        String lockKey = "event:" + eventId + ":lock";
        RLock lock = redissonClient.getLock(lockKey);

        boolean isLockAcquired = lock.tryLock(5, 10, TimeUnit.SECONDS);
        assertThat(isLockAcquired).isTrue();
        System.out.println("락이 성공적으로 획득되었습니다.");

        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            System.out.println("락이 성공적으로 해제되었습니다.");
        }

        System.out.println("테스트 종료: 락 획득 성공 확인");
    }

    @Test
    @DisplayName("6. 락 획득 실패 확인")
    public void shouldFailToAcquireLockWhenAlreadyLocked() throws InterruptedException {
        System.out.println("테스트 시작: 락 획득 실패 확인");

        String lockKey = "event:" + eventId + ":lock";
        RLock lock = redissonClient.getLock(lockKey);

        boolean isLockAcquiredByFirstThread = lock.tryLock(5, 10, TimeUnit.SECONDS);
        assertThat(isLockAcquiredByFirstThread).isTrue();
        System.out.println("첫 번째 스레드가 락을 성공적으로 획득했습니다.");

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        CountDownLatch latch = new CountDownLatch(1);

        executorService.submit(() -> {
            try {
                boolean isLockAcquiredBySecondThread = lock.tryLock(2, 5, TimeUnit.SECONDS);
                assertThat(isLockAcquiredBySecondThread).isFalse();
                System.out.println("두 번째 스레드는 락을 획득하지 못했습니다.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown();
            }
        });

        latch.await();
        executorService.shutdown();

        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            System.out.println("첫 번째 스레드가 락을 성공적으로 해제했습니다.");
        }

        System.out.println("테스트 종료: 락 획득 실패 확인");
    }


    private User getUserOrThrow(Long userId) {
        String uniqueEmail = "testUser" + userId + System.nanoTime() + "@example.com";
        String uniqueNickname = "User" + userId + System.nanoTime();

        return User.builder()
                .id(userId)
                .email(uniqueEmail)
                .password("passworda!")
                .userRole(UserRole.ROLE_USER)
                .nickname(uniqueNickname)
                .build();
    }

    private Gathering getGatheringOrThrow(Long gatheringId) {
        return Gathering.of("테스트 소모임", 100, "테스트 소모임 설명입니다.");
    }
}
