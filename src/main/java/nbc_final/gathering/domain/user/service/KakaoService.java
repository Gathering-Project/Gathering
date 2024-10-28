package nbc_final.gathering.domain.user.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc_final.gathering.common.config.JwtUtil;
import nbc_final.gathering.common.exception.ResponseCode;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.domain.user.dto.KakaoUserInfoDto;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class KakaoService {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Value("${kakao.client.id}")
    private String clientId;

    @Value("${kakao.redirect.url}")
    private String redirectUri;

    // 클라이언트 설정을 반환하는 메서드
    public Map<String, String> getKakaoConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("clientId", clientId);
        config.put("redirectUrl", redirectUri);
        return config;
    }

    public LoginResponseDto kakaoLogin(String code, HttpServletResponse response) {
        String accessToken = getAccessToken(code);
        KakaoUserInfoDto userInfo = getUserInfo(accessToken);
        String jwtToken = registerOrLoginKakaoUser(userInfo, response);
        return new LoginResponseDto(jwtToken);
    }

    // 카카오 서버에서 액세스 토큰을 받기 위한 메서드
    private String getAccessToken(String code) {
        log.info("인가코드 : " + code);
        URI uri = UriComponentsBuilder
                .fromUriString("https://kauth.kakao.com")
                .path("/oauth/token")
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("code", code)
                .build()
                .toUri();

        Map<String, Object> response = restTemplate.postForObject(uri, null, Map.class);
        if (response == null || !response.containsKey("access_token")) {
            throw new ResponseCodeException(ResponseCode.INVALID_TOKEN);
        }
        return (String) response.get("access_token");
    }

    // 액세스 토큰을 이용해 카카오 사용자 정보를 가져오는 메서드
    private KakaoUserInfoDto getUserInfo(String accessToken) {
        log.info("accessToken : " + accessToken);
        URI uri = URI.create("https://kapi.kakao.com/v2/user/me");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        Map<String, Object> response = restTemplate.exchange(uri, HttpMethod.GET, entity, Map.class).getBody();
        if (response == null || !response.containsKey("id")) {
            throw new ResponseCodeException(ResponseCode.INVALID_TOKEN);
        }

        Long id = ((Number) response.get("id")).longValue();
        Map<String, Object> properties = (Map<String, Object>) response.get("properties");
        Map<String, Object> kakaoAccount = (Map<String, Object>) response.get("kakao_account");

        String nickname = (String) properties.get("nickname");
        String email = (String) kakaoAccount.get("email");

        return new KakaoUserInfoDto(id, nickname, email);
    }

    // 카카오 사용자 정보로 회원 가입 또는 로그인 처리하는 메서드
    private String registerOrLoginKakaoUser(KakaoUserInfoDto userInfo, HttpServletResponse response) {
        Optional<User> existingUserByEmail = userRepository.findByEmail(userInfo.getEmail());

        if (existingUserByEmail.isPresent()) {
            User existingUser = existingUserByEmail.get();
            if (existingUser.getKakaoId() == null) {
                // 기존 이메일 계정에 kakaoId 추가
                existingUser.updateKakaoId(userInfo.getId());
                userRepository.save(existingUser);
            }
            // 기존 유저 로그인 처리
            return jwtUtil.createToken(existingUser.getId(), existingUser.getEmail(), existingUser.getUserRole(), existingUser.getNickname());
        } else {
            // 신규 카카오 유저 회원 가입
            User newUser = registerNewKakaoUser(userInfo);
            return jwtUtil.createToken(newUser.getId(), newUser.getEmail(), newUser.getUserRole(), newUser.getNickname());
        }
    }

    // 신규 카카오 사용자 회원 가입 처리
    private User registerNewKakaoUser(KakaoUserInfoDto userInfo) {
        String encodedPassword = passwordEncoder.encode(UUID.randomUUID().toString());
        String uniqueNickname = GenerateRandomNickname.generateNickname();
        User newUser = User.builder()
                .email(userInfo.getEmail())
                .password(encodedPassword)
                .nickname(uniqueNickname)
                .userRole(UserRole.ROLE_USER)
                .kakaoId(userInfo.getId())
                .build();
        return userRepository.save(newUser);
    }
}
