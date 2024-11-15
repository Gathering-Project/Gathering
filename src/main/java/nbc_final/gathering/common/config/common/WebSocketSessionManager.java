package nbc_final.gathering.common.config.common;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
// 웹소켓 연결 상태로 로그인 세션 관리
public class WebSocketSessionManager {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String WEBSOCKET_SESSION_KEY_PREFIX = "websocket:userId:";

    // Redis에 로그인 이후 웹소결 연결 세션 저장 (만료 시간 설정 가능)
    public void addUserSession(Long userId, String sessionId) {
        redisTemplate.opsForValue().set(WEBSOCKET_SESSION_KEY_PREFIX + userId, sessionId, 60, TimeUnit.MINUTES);
    }

    // Redis에서 로그아웃 이웃 웹소켓 연결 세션 제거
    public void removeUserSession(Long userId) {
        redisTemplate.delete(WEBSOCKET_SESSION_KEY_PREFIX + userId);
    }

    // 특정 사용자에게 메시지 전송 (세션을 Redis에서 검색)
    public String getSessionId(Long userId) {
        return redisTemplate.opsForValue().get(WEBSOCKET_SESSION_KEY_PREFIX + userId);
    }
}
