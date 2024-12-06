package nbc_final.gathering.common.config.common;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class WebSocketSessionManager {

    private static final String WEBSOCKET_SESSION_KEY_PREFIX = "websocket:session:";
    private final RedisTemplate<String, String> redisTemplate;

    // Redis에 세션 저장 (만료 시간 설정 가능)
    public void addUserSession(Long userId, String sessionId) {
        redisTemplate.opsForValue().set(WEBSOCKET_SESSION_KEY_PREFIX + userId, sessionId, 60, TimeUnit.MINUTES);
    }

}
