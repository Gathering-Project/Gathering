package nbc_final.gathering.domain.gathering.ranking;

import nbc_final.gathering.common.config.RedisMockConfig;
import nbc_final.gathering.common.config.redis.RedisLimiter;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.gathering.repository.GatheringRepository;
import nbc_final.gathering.domain.gathering.service.GatheringService;
import nbc_final.gathering.domain.member.entity.Member;
import nbc_final.gathering.domain.member.enums.MemberRole;
import nbc_final.gathering.domain.member.enums.MemberStatus;
import nbc_final.gathering.domain.member.repository.MemberRepository;
import nbc_final.gathering.domain.user.entity.User;
import nbc_final.gathering.domain.user.enums.UserRole;
import nbc_final.gathering.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(RedisMockConfig.class)
public class GatheringServiceTest {

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    private RedisLimiter redisLimiter;

    @MockBean
    private RedissonClient redissonClient;

    @MockBean
    private ZSetOperations<String, Object> zSetOps;

    @MockBean
    private SetOperations<String, Object> setOps;

    @Autowired
    private GatheringService gatheringService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GatheringRepository gatheringRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Gathering testGathering;
    private User testUser;
    private AuthUser authUser;
    private Member testMember;


    @BeforeEach
    void setUp() throws InterruptedException {

        // Mock 객체 초기화
        setOps = Mockito.mock(SetOperations.class);
        zSetOps = Mockito.mock(ZSetOperations.class);
        redisTemplate = Mockito.mock(RedisTemplate.class);

        // RedisTemplate Mock 설정
        when(redisTemplate.opsForSet()).thenReturn(setOps);


        // SetOperations Mock 동작 정의
        when(setOps.add(anyString(), any())).thenReturn(1L);
        when(setOps.size(anyString())).thenReturn(1L);

        // ZSetOperations Mock 동작 정의
        when(zSetOps.reverseRangeWithScores(eq(GatheringService.TODAY_RANKING_KEY), eq(0L), eq(2L)))
                .thenReturn(Set.of(
                        new MockTypedTuple<>("Gathering A", 100.0),
                        new MockTypedTuple<>("Gathering B", 80.0),
                        new MockTypedTuple<>("Gathering C", 60.0)
                ));

        // RedisLimiter Mock 설정
        redisLimiter = Mockito.mock(RedisLimiter.class);
        when(redisLimiter.isAllowed(any(AuthUser.class), anyString(), anyInt(), anyLong())).thenReturn(true);

        // 테스트 데이터 생성 및 저장
        testUser = new User();
        testUser.setId(1L);
        testUser.setPassword("abcd123@");
        testUser.setEmail("test@example.com");
        testUser.setProfileImagePath("test-image-path.jpg");
        testUser.setUserRole(UserRole.ROLE_USER);
        testUser = userRepository.saveAndFlush(testUser);

        testGathering = new Gathering(testUser.getId(), "test Title", "test description",
                1, 30, BigDecimal.valueOf(4), "test location");
        testGathering.setId(1L);
        testGathering.setTotalGatheringViewCount(0);
        testGathering.setDisplayDate(LocalDate.now());
        gatheringRepository.save(testGathering);

        authUser = new AuthUser(testUser.getId(), testUser.getEmail(), testUser.getUserRole(), "testNick");
        testMember = new Member(testUser, testGathering, MemberRole.HOST, MemberStatus.APPROVED);
        memberRepository.saveAndFlush(testMember);

        testGathering.getMembers().add(testMember);
        gatheringRepository.saveAndFlush(testGathering);

        String todayGatheringViewSetKey = "todayGatheringSet:" + testGathering.getId();
        redisTemplate.delete(todayGatheringViewSetKey);
    }

    @Test
    public void testRedisMockConfig() {
        // Assert that redisTemplate is not null and is the mocked bean
        assertNotNull(redisTemplate);
        assertTrue(Mockito.mockingDetails(redisTemplate).isMock());
    }

    @Test
    void testRedisLimiter() {
        boolean isAllowed = redisLimiter.isAllowed(authUser, "getGathering" + testGathering.getId(), 10, 5);
        assertTrue(isAllowed, "RedisLimiter should allow the request");
    }

//    @Test
//    @DisplayName("소모임 단건 조회 및 조회수 증가 테스트")
//    void testGetGatheringAndViewCountUpdate() {
//        // Arrange
//        String todayGatheringViewSetKey = "todayGatheringSet:" + testGathering.getId();
//
//        // Act - 첫 번째 요청
//        gatheringService.getGathering(authUser, testGathering.getId());
//
//        // Assert - 첫 번째 요청 검증
//        verify(redisLimiter, times(1)).isAllowed(authUser, "getGathering" + testGathering.getId(), 10, 5);
//        verify(setOps, times(1)).add(eq(todayGatheringViewSetKey), eq(authUser.getUserId()));
//        verify(setOps, times(1)).size(eq(todayGatheringViewSetKey));
//
//        // Act - 두 번째 요청 (중복 요청 방지 테스트)
//        gatheringService.getGathering(authUser, testGathering.getId());
//
//        // Assert - 중복 요청 검증
//        // Redis Set에 유저 ID 추가는 최초 한 번만 호출되어야 함
//        verify(setOps, times(1)).add(eq(todayGatheringViewSetKey), eq(authUser.getUserId()));
//
//        // Redis Set 크기 조회는 호출될 수 있음
//        verify(setOps, times(2)).size(eq(todayGatheringViewSetKey)); // 두 번째 요청에서도 크기만 조회
//    }

//    @Test
//    @DisplayName("랜덤 데이터로 인기 소모임 랭킹 조회 테스트")
//    void testGetTopViewGatheringListWithRandomData() {
//
//        // Arrange
//        Random random = new Random(42);
//
//        // Mock 데이터 생성
//        Set<ZSetOperations.TypedTuple<Object>> mockData = Set.of(
//                new MockTypedTuple<>("Gathering A", (double) random.nextInt(100) + 1),
//                new MockTypedTuple<>("Gathering B", (double) random.nextInt(100) + 1),
//                new MockTypedTuple<>("Gathering C", (double) random.nextInt(100) + 1)
//        );
//
//        // ZSetOperations Mock 설정
//        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
//        when(zSetOps.reverseRangeWithScores(eq(GatheringService.TODAY_RANKING_KEY), eq(0L), eq(2L)))
//                .thenReturn(mockData);
//
//        // Act
//        Map<String, Integer> topGatherings = gatheringService.getTopViewGatheringList();
//
//        // Assert
//        assertNotNull(topGatherings, "Top gatherings list should not be null");
//        assertEquals(3, topGatherings.size(), "Top gatherings list size should be 3");
//
//        mockData.forEach(tuple -> {
//            String title = tuple.getValue().toString();
//            Integer score = tuple.getScore().intValue();
//            assertTrue(topGatherings.containsKey(title), "The title should exist in the topGatherings map");
//            assertEquals(score, topGatherings.get(title), "The score should match the mock data");
//        });
//
//        // Mock 호출 검증
//        verify(redisTemplate, times(1)).opsForZSet();
//        verify(zSetOps, times(1)).reverseRangeWithScores(eq(GatheringService.TODAY_RANKING_KEY), eq(0L), eq(2L));
//    }
//
//
//    // Mock ZSetOperations.TypedTuple 구현
//    private static class MockTypedTuple<V> implements ZSetOperations.TypedTuple<V> {
//        private final V value;
//        private final Double score;
//
//        MockTypedTuple(V value, Double score) {
//            this.value = value;
//            this.score = score;
//        }
//
//        @Override
//        public V getValue() {
//            return value;
//        }
//
//        @Override
//        public Double getScore() {
//            return score;
//        }
//
//        @Override
//        public int compareTo(ZSetOperations.TypedTuple<V> o) {
//            return Double.compare(o.getScore(), this.score);
//        }
//    }

}