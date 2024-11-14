package nbc_final.gathering.common.config.chatconfig;

import lombok.RequiredArgsConstructor;
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

    @Value("${rabbitmq_host}")
    private String host;
    @Value("${rabbitmq_relay_port}")
    private int port;
    @Value("${rabbitmq_relay_system-login}")
    private String systemLogin;
    @Value("${rabbitmq_relay_client-passcode}")
    private String systemPasscode;
    @Value("${rabbitmq_relay_client-login}")
    private String clientLogin;
    @Value("${rabbitmq_relay_client-passcode}")
    private String clientPasscode;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // socketJs 클라이언트가 WebSocket 핸드셰이크를 하기 위해 연결할 endpoint를 지정할 수 있다.
        registry.addEndpoint("/chat/inbox")
                .setAllowedOriginPatterns("*") // cors 허용을 위해 꼭 설정해주어야 함. setCredential() 설정시에 AllowedOrigin 과 같이 사용될 경우 오류가 날 수 있으므로 OriginPatterns 설정으로 사용하였음
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지 브로커 설정
        registry.setPathMatcher(new AntPathMatcher(".")); // url을 chat/room/3 -> chat.room.3으로 참조하기 위한 설정

        registry.enableStompBrokerRelay("/queue", "/topic", "/exchange", "/amq/queue")
                .setRelayHost(host)
                .setRelayPort(port)
                .setSystemLogin(systemLogin)
                .setSystemPasscode(systemPasscode)
                .setClientLogin(clientLogin)
                .setClientPasscode(clientPasscode);

        // 클라이언트로부터 메시지를 받을 api의 prefix를 설정함
        // publish
        registry.setApplicationDestinationPrefixes("/pub");

    }
}