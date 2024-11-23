//package nbc_final.gathering.common.config.jwt;
//
//import io.jsonwebtoken.Claims;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.server.ServerHttpRequest;
//import org.springframework.http.server.ServerHttpResponse;
//import org.springframework.http.server.ServletServerHttpRequest;
//import org.springframework.web.socket.WebSocketHandler;
//import org.springframework.web.socket.server.HandshakeInterceptor;
//
//import java.util.Map;
//
//@Slf4j
//public class JwtHandshakeInterceptor implements HandshakeInterceptor {
//
//    private final JwtUtil jwtUtil;
//
//    public JwtHandshakeInterceptor(JwtUtil jwtUtil) {
//        this.jwtUtil = jwtUtil;
//    }
//
//    @Override
//    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
//                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
//        String token = ((ServletServerHttpRequest) request).getServletRequest().getHeader("Authorization");
//
//        log.info("WebSocket 핸드셰이크 시도 감지");
//
//        if (token != null && token.startsWith("Bearer ")) {
//            token = token.substring(7); // "Bearer " 제거
//            log.info("토큰이 발견되었습니다. 검증 시작: {}", token);
//
//            if (jwtUtil.validateToken(token)) {
//                Claims claims = jwtUtil.getUserInfoFromToken(token);
//                attributes.put("userId", claims.getSubject());
//                log.info("토큰이 성공적으로 검증되었습니다. 유저 ID: {}", claims.getSubject());
//                return true;
//            } else {
//                log.warn("토큰 검증에 실패했습니다");
//            }
//        } else {
//            log.warn("Authorization 헤더가 없거나 Bearer로 시작하지 않습니다");
//        }
//
//        response.setStatusCode(HttpStatus.FORBIDDEN);
//        return false;
//    }
//
//    @Override
//    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
//                               WebSocketHandler wsHandler, Exception exception) {
//        log.info("WebSocket afterHandshake가 실행되었습니다. 예외: {}", exception != null ? exception.getMessage() : "없음");
//    }
//}
