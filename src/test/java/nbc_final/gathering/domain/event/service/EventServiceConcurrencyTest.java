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

        // 소모임 엔티티를 생성하고 데이터베이스에 저장
        Gathering gathering = gatheringRepository.save(getGatheringOrThrow(1L));
        gatheringId = gathering.getId();
        System.out.println("소모임 " + gatheringId + "이 생성되었습니다.");

        // 테스트용 사용자 생성 및 게더링 멤버로 등록
        for (int i = 1; i <= 105; i++) {
            User user = userRepository.save(getUserOrThrow((long) i));  // User 저장
            gathering.addMember(user, MemberRole.GUEST, MemberStatus.APPROVED);  // 멤버로 추가
            System.out.println("사용자 " + i + "가 생성되고 게더링 멤버로 추가되었습니다.");
        }

        // Gathering을 다시 저장하여 members 관계가 DB에 반영되도록 설정
        gatheringRepository.save(gathering);

        // Event 생성
        User eventCreator = getUserOrThrow(userId);
        userRepository.save(eventCreator); // 반드시 저장 후 사용

        EventCreateRequestDto requestDto = EventCreateRequestDto.of(
                "Test Event",
                "Test Description",
                "2024-12-31",
                "Test Location",
                100
        );

        Event event = Event.of(requestDto.getTitle(), requestDto.getDescription(), requestDto.getDate(),
                requestDto.getLocation(), requestDto.getMaxParticipants(), gathering, eventCreator);
        eventRepository.save(event); // 이벤트 저장
        eventId = event.getId();
        System.out.println("이벤트 " + eventId + "가 생성되었습니다.");
    }

    @Test
    @DisplayName("100명의 동시 참가 시, 최대 참가자 수 내에서 정상 처리")
    public void shouldAllowConcurrentParticipationWithinLimit() throws InterruptedException {
        int concurrentUsers = 100; // 동시 참여할 사용자 수
        ExecutorService executorService = Executors.newFixedThreadPool(concurrentUsers);
        CountDownLatch latch = new CountDownLatch(concurrentUsers);

        System.out.println("동시 참가 테스트 시작: " + concurrentUsers + "명의 사용자");

        for (int i = 0; i < concurrentUsers; i++) {
            long testUserId = i + 2; // 각기 다른 사용자 ID
            executorService.submit(() -> {
                try {
                    eventService.participateInEventWithDistributedLock(testUserId, gatheringId, eventId);
                    System.out.println("사용자 " + testUserId + "가 이벤트 " + eventId + "에 성공적으로 참여했습니다.");
                } catch (ResponseCodeException e) {
                    System.out.println("사용자 " + testUserId + "가 이벤트 " + eventId + " 참여에 실패했습니다: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        Event event = eventRepository.findById(eventId).orElseThrow();
        System.out.println("테스트 완료: 현재 참가자 수 = " + event.getCurrentParticipants());
        assertThat(event.getCurrentParticipants()).isLessThanOrEqualTo(event.getMaxParticipants());
    }

    @Test
    @DisplayName("참가 인원을 초과할 경우, 예외 발생 확인")
    public void shouldThrowExceptionWhenParticipationExceedsLimit() {
        int overLimitUsers = 105; // 제한 인원보다 많은 사용자 수
        System.out.println("초과 참여 테스트 시작: " + overLimitUsers + "명의 사용자");

        for (int i = 0; i < overLimitUsers; i++) {
            final long testUserId = i + 2;

            // 테스트를 위한 사용자 미리 생성
            userRepository.save(getUserOrThrow(testUserId));

            if (i < 100) {
                eventService.participateInEventWithDistributedLock(testUserId, gatheringId, eventId);
                System.out.println("사용자 " + testUserId + "가 이벤트 " + eventId + "에 성공적으로 참여했습니다.");
            } else {
                System.out.println("참가자 수를 초과하여 사용자 " + testUserId + "가 참여를 시도합니다.");
                assertThrows(ResponseCodeException.class, () -> {
                    eventService.participateInEventWithDistributedLock(testUserId, gatheringId, eventId);
                    System.out.println("사용자 " + testUserId + "는 이벤트 " + eventId + "에 참여할 수 없어야 합니다.");
                });
            }
        }

        System.out.println("초과 참여 테스트 완료");
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
