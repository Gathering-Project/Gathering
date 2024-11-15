package nbc_final.gathering.common.config.common;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class WebSocketSessionManager {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String WEBSOCKET_SESSION_KEY_PREFIX = "websocket:session:";

    // Redis에 세션 저장 (만료 시간 설정 가능)
    public void addUserSession(Long userId, String sessionId) {
        redisTemplate.opsForValue().set(WEBSOCKET_SESSION_KEY_PREFIX + userId, sessionId, 60, TimeUnit.MINUTES);
    }

    // Redis에서 세션 제거
    public void removeUserSession(Long userId) {
        redisTemplate.delete(WEBSOCKET_SESSION_KEY_PREFIX + userId);
    }

    // 특정 사용자에게 메시지 전송 (세션을 Redis에서 검색)
    public String getSessionId(Long userId) {
        return redisTemplate.opsForValue().get(WEBSOCKET_SESSION_KEY_PREFIX + userId);
    }
}
