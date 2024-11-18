package nbc_final.gathering.common.config.redis;

import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.dto.AuthUser;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisLimiter {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 특정 사용자와 API 경로에 대해 주어진 시간 내에 최대 요청 횟수를 초과하지 않도록 제한합니다.
     *
     * @param authUser          요청을 보내는 사용자 객체
     * @param actionKey         특정 API 경로 또는 액션 식별자
     * @param maxRequests       허용되는 최대 요청 수
     * @param timeWindowSeconds 요청 수를 추적하는 시간 창(초 단위)
     * @return 요청이 허용되면 true, 제한을 초과하면 false를 반환
     */
    public boolean isAllowed(AuthUser authUser, String actionKey, int maxRequests, long timeWindowSeconds) {
        String key = "rate_limit:" + authUser.getUserId() + ":" + actionKey;
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();

        Integer requestCount = (Integer) operations.get(key);

        if (requestCount == null) {
            // 처음 요청일 경우, 카운터 초기화 및 만료 시간 설정
            operations.set(key, 1, timeWindowSeconds, TimeUnit.SECONDS);
            return true;
        } else if (requestCount < maxRequests) {
            // 최대 요청 수 미만일 경우, 카운터 증가
            operations.increment(key);
            return true;
        } else {
            // 최대 요청 수를 초과한 경우 제한
            return false;
        }
    }
}
