package nbc_final.gathering.domain.gathering.service;

import lombok.RequiredArgsConstructor;
import nbc_final.gathering.domain.gathering.entity.Gathering;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class GatheringViewCounterResetScheduler {

    private final RedisTemplate<String, Object> redisTemplate;

    private final GatheringService gatheringService;

    // 자정마다 일일 랭킹, 일일 조회수 초기화
    @Scheduled(cron = "0 0 0 * * ?")
    public void resetGatheringViewCounts() {
        System.out.println("소모임 조회수 초기화");

        // 초기화 전에 쌓인 조회수 털기
        resetSnapshotKeys();
        // "todayCardViewSet, todayCardRanking 초기화"
        Set<String> keysToDelete = redisTemplate.keys("todayGatheringViewSet:*");
        keysToDelete.addAll(redisTemplate.keys("todayGatheringRanking"));

        if (!keysToDelete.isEmpty()) {
            redisTemplate.delete(keysToDelete);
        }
    }

    // 9시부터 21시까지 1시간 마다 snapshot 초기화
    @Scheduled(cron = "0 0  9-22 * * ?")
    public void resetSnapshotKeys() {
        System.out.println("card:snapshot 키 초기화");

        // "card:snapshot:*" 모든 키 조회
        Set<String> keysToDelete = redisTemplate.keys("card:snapshot(1h):*");

        if (keysToDelete != null && !keysToDelete.isEmpty()) {
            for (String key : keysToDelete) {
                // key: "card:snapshot:{cardId}"에서 cardId 추출
                Long gatheringId = extractGatheringIdFromKey(key);

                // Redis 에서 해당 key 의 조회수 값 가져오기
                Integer snapshotViewCount = (Integer) redisTemplate.opsForValue().get(key);

                if (snapshotViewCount != null) {
                    // Gathering 조회
                    Gathering gathering = gatheringService.findGathering(gatheringId);

                    // totalCardViewCount 에 snapshot 조회수를 더함
                    gathering.updateTotalGatheirngViewCount(gathering.getTotalGatheringViewCount() + snapshotViewCount);

                    // gathering 객체 저장
                    gatheringService.saveGathering(gathering);
                }
            }
            GatheringService.previousViewCount = 0L;

            // Redis 에서 모든 snapshot 키 삭제
            redisTemplate.delete(keysToDelete);
            System.out.println(keysToDelete.size() + "개의 snapshot 키가 삭제되었습니다.");
        }
    }

    // "gathering:snapshot:{gatheringId}"에서 gatheringId 추출 메서드
    private Long extractGatheringIdFromKey (String key ){
        return Long.valueOf(key.split(":")[2]);
    }
}
