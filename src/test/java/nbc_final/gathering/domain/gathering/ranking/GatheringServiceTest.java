package nbc_final.gathering.domain.gathering.ranking;

import nbc_final.gathering.common.config.RedisMockConfig;
import nbc_final.gathering.common.config.redis.RedisLimiter;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.domain.gathering.dto.response.GatheringWithCountResponseDto;
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
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static nbc_final.gathering.domain.gathering.service.GatheringService.TODAY_RANKING_KEY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
@Import(RedisMockConfig.class)
public class GatheringServiceTest {

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    private RedisLimiter redisLimiter;

    @Autowired
    private RedissonClient redissonClient;

    @MockBean
    private ZSetOperations<String, Object> zSetOps;

    @MockBean
    private SetOperations<String, Object> setOps;

    @InjectMocks
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
    void setUp() {

        // RedissonClient의 동작 Mock 설정
        RLock mockLock = Mockito.mock(RLock.class);
        when(redissonClient.getLock(anyString())).thenReturn(mockLock);

        // SetOperations Mock 설정
        setOps = Mockito.mock(SetOperations.class);
        when(redisTemplate.opsForSet()).thenReturn(setOps);

        // ZSetOperations Mock 설정
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);

        // RedisLimiter Mock 설정
        when(redisLimiter.isAllowed(any(AuthUser.class), anyString(), anyInt(), anyLong())).thenReturn(true);

//        // ZSetOperations Mock 데이터 설정
//        when(zSetOps.reverseRangeWithScores(anyString(), eq(0), eq(2)))
//                .thenReturn(Set.of(
//                        createTypedTuple("Gathering A", 100.0),
//                        createTypedTuple("Gathering B", 80.0),
//                        createTypedTuple("Gathering C", 60.0)
//                ));

        // 테스트 유저 생성 및 저장
        testUser = new User();
        testUser.setId(1L);
        testUser.setPassword("abcd123@");
        testUser.setEmail("test@example.com");
        testUser.setProfileImagePath("test-image-path.jpg");
        testUser.setUserRole(UserRole.ROLE_USER);
        testUser = userRepository.saveAndFlush(testUser);

        // 테스트 소모임 생성 및 저장
        testGathering = new Gathering(testUser.getId(), "test Title", "test description",
                1, 30, BigDecimal.valueOf(4), "test location");
        testGathering.setId(1L);
        testGathering.setTotalGatheringViewCount(0);
        testGathering.setDisplayDate(LocalDate.now());
        gatheringRepository.save(testGathering);

        // AuthUser 및 Member 생성
        authUser = new AuthUser(testUser.getId(), testUser.getEmail(), testUser.getUserRole(), "testNick");
        testMember = new Member(testUser, testGathering, MemberRole.HOST, MemberStatus.APPROVED);
        memberRepository.saveAndFlush(testMember);

        // 소모임에 멤버 추가
        testGathering.getMembers().add(testMember);
        gatheringRepository.saveAndFlush(testGathering);
    }

    // ZSetOperations.TypedTuple<Object> 객체 생성하는 helper method 수정
    private ZSetOperations.TypedTuple<String> createTypedTuple(String value, Double score) {
        return new ZSetOperations.TypedTuple<String>() {
            @Override
            public int compareTo(ZSetOperations.TypedTuple<String> o) {
                return 0;
            }

            @Override
            public String getValue() {
                return value;
            }

            @Override
            public Double getScore() {
                return score;
            }
        };
    }



}