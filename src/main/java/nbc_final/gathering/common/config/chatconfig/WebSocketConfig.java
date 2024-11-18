package nbc_final.gathering.common.config.chatconfig;

import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.config.jwt.JwtUtil;
import nbc_final.gathering.common.config.jwt.JwtHandshakeInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtUtil jwtUtil;
    @Value("${rabbitmq_host}")
    private String host;
    @Value("${rabbitmq_relay_port}")
    private int port;
    @Value("${rabbitmq_relay_system_login}")
    private String systemLogin;
    @Value("${rabbitmq_relay_client_passcode}")
    private String systemPasscode;
    @Value("${rabbitmq_relay_client_login}")
    private String clientLogin;
    @Value("${rabbitmq_relay_client_passcode}")
    private String clientPasscode;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결을 위한 STOMP 엔드포인트 설정
        registry.addEndpoint("/gathering/inbox") // 클라이언트가 연결할 WebSocket 엔드포인트를 정의
                .setAllowedOriginPatterns("*") // CORS를 허용하기 위해 모든 오리진을 허용
                .addInterceptors(new JwtHandshakeInterceptor(jwtUtil)); // WebSocket 연결 시 JWT 인증 인터셉터 추가
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지 브로커 설정
        registry.setPathMatcher(new AntPathMatcher(".")); // 경로 구분자를 점(.)으로 설정 (예: chat/room/3 -> chat.room.3)

        // STOMP 브로커 리레이를 활성화하여 RabbitMQ와 연결
        registry.enableStompBrokerRelay("/queue", "/topic", "/exchange", "/amq/queue") // 브로커에서 사용할 목적지 프리픽스 설정
                .setRelayHost(host) // RabbitMQ의 호스트 주소 설정
                .setRelayPort(port) // RabbitMQ의 리레이 포트 설정
                .setSystemLogin(systemLogin) // RabbitMQ 시스템 사용자 로그인 설정
                .setSystemPasscode(systemPasscode) // RabbitMQ 시스템 사용자 비밀번호 설정
                .setClientLogin(clientLogin) // RabbitMQ 클라이언트 사용자 로그인 설정
                .setClientPasscode(clientPasscode); // RabbitMQ 클라이언트 사용자 비밀번호 설정

        // 클라이언트에서 메시지를 보낼 때 사용할 prefix 설정
        registry.setApplicationDestinationPrefixes("/pub"); // 클라이언트가 메시지를 전송할 경로의 접두어
    }


}
