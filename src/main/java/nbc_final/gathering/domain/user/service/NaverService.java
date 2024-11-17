package nbc_final.gathering.domain.user.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc_final.gathering.common.config.JwtUtil;
import nbc_final.gathering.common.config.common.WebSocketSessionManager;
import nbc_final.gathering.common.exception.ResponseCode;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.domain.user.dto.NaverUserInfoDto;
import nbc_final.gathering.domain.user.dto.response.LoginResponseDto;
import nbc_final.gathering.domain.user.entity.User;
import nbc_final.gathering.domain.user.enums.UserRole;
import nbc_final.gathering.domain.user.repository.UserRepository;
import nbc_final.gathering.domain.user.utils.GenerateRandomNickname;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class NaverService {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final WebSocketSessionManager webSocketSessionManager;

    @Value("${NAVER_CLIENT_ID}")
    private String clientId;

    @Value("${NAVER_CLIENT_SECRET}")
    private String clientSecret;

    @Value("${NAVER_REDIRECT_URL}")
    private String redirectUri;

    // 상태 값 생성 메서드
    private String generateState() {
        SecureRandom random = new SecureRandom();
        byte[] stateBytes = new byte[10];
        random.nextBytes(stateBytes);
        return Base64.getUrlEncoder().encodeToString(stateBytes);
    }

    // 엑세스 토큰 요청 메서드
    public String getAccessToken(String code, String state) {
        URI uri = UriComponentsBuilder.fromUriString("https://nid.naver.com/oauth2.0/token")
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .queryParam("code", code)
                .queryParam("state", state)
                .build().toUri();

        try {
            Map<String, Object> response = restTemplate.postForObject(uri, null, Map.class);
            if (response == null || !response.containsKey("access_token")) {
                throw new ResponseCodeException(ResponseCode.INVALID_TOKEN);
            }
            return (String) response.get("access_token");
        } catch (HttpClientErrorException e) {
            log.error("네이버 API에서 액세스 토큰을 가져오는 데 실패: {}", e.getMessage());
            throw new ResponseCodeException(ResponseCode.INVALID_TOKEN);
        }
    }

    // 사용자 정보 가져오기 메서드
    public NaverUserInfoDto getUserInfo(String accessToken) {
        URI uri = URI.create("https://openapi.naver.com/v1/nid/me");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            Map<String, Object> response = restTemplate.exchange(uri, HttpMethod.GET, entity, Map.class).getBody();
            if (response == null || !response.containsKey("response")) {
                throw new ResponseCodeException(ResponseCode.INVALID_TOKEN);
            }

            Map<String, Object> userInfo = (Map<String, Object>) response.get("response");
            String id = (String) userInfo.get("id");
            String nickname = (String) userInfo.get("nickname");
            String email = (String) userInfo.get("email");

            return NaverUserInfoDto.of(id, nickname, email);
        } catch (HttpClientErrorException e) {
            log.error("네이버 API에서 사용자 정보를 가져오는 데 실패: {}", e.getMessage());
            throw new ResponseCodeException(ResponseCode.INVALID_TOKEN);
        }
    }

    // 네이버 사용자 로그인 처리
    public LoginResponseDto naverLogin(String code, String state, HttpServletResponse response) {
        log.info("네이버 로그인 시작 - code: {}, state: {}", code, state);

        String accessToken = getAccessToken(code, state);
        log.info("액세스 토큰 성공적으로 획득: {}", accessToken);

        NaverUserInfoDto userInfo = getUserInfo(accessToken);
        log.info("네이버 사용자 정보 획득 성공: {}", userInfo);

        Optional<User> existingUser = userRepository.findByEmail(userInfo.getEmail());
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            if (user.getNaverId() == null) {
                user.updateNaverId(userInfo.getId());
                userRepository.save(user);
            }
        } else {
            String randomPassword = UUID.randomUUID().toString();
            String encodedPassword = passwordEncoder.encode(randomPassword);
            String randomNickname = GenerateRandomNickname.generateNickname();

            user = new User(userInfo.getEmail(), encodedPassword, UserRole.ROLE_USER, randomNickname);
            user.updateNaverId(userInfo.getId());
            userRepository.save(user);
        }

        String jwtToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole(), user.getNickname());
//        jwtUtil.addJwtToCookie(jwtToken, response);
        log.info("JWT 토큰 생성 및 쿠키에 추가 완료: {}", jwtToken);

        // WebSocket 세션 ID 생성 및 Redis 저장
        String websocketSessionId = generateWebSocketSessionId(user.getId());
        webSocketSessionManager.addUserSession(user.getId(), websocketSessionId);

        // 클라이언트가 WebSocket 연결을 수행할 수 있는 URL 제공
        String websocketUrl = "ws://localhost:8080/gathering/inbox?token=" + jwtToken;

        return new LoginResponseDto(jwtToken, websocketUrl);
    }

    // 네이버 연동 해제 메서드
    public void expireNaverAccessToken(String accessToken) {
        URI uri = UriComponentsBuilder.fromUriString("https://nid.naver.com/oauth2.0/token")
                .queryParam("grant_type", "delete")
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .queryParam("access_token", accessToken)
                .queryParam("service_provider", "NAVER")
                .build().toUri();

        try {
            Map<String, Object> response = restTemplate.postForObject(uri, null, Map.class);
            if ("success".equals(response.get("result"))) {
                log.info("네이버 연동 해제 성공");
            } else {
                log.warn("네이버 연동 해제 요청 실패, 응답: {}", response);
                throw new ResponseCodeException(ResponseCode.UNLINK_FAILED);
            }
        } catch (HttpClientErrorException e) {
            log.error("네이버 연동 해제 실패: {}", e.getMessage());
            throw new ResponseCodeException(ResponseCode.INVALID_TOKEN);
        }
    }

    private String generateWebSocketSessionId(Long userId) {
        return "ws-session-" + userId;
    }
}
