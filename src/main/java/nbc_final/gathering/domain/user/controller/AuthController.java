package nbc_final.gathering.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Naver, Kakao OAuth 2.0 API", description = "Naver, Kakao OAuth 관련 API 입니다.")
public class AuthController {

    @Value("${KAKAO_CLIENT_ID}")
    private String clientId; // 환경변수에서 클라이언트 ID 가져오기

    @Value("${KAKAO_REDIRECT_URL}")
    private String redirectUrl; // 환경변수에서 리다이렉트 URL 가져오기

    @GetMapping("/kakao-url")
    @Operation(summary = "Kakao-url", description = "OAuth 인증에 필요한 각 유저의 Kakao-url 출력 API입니다.")
    public ResponseEntity<String> getKakaoAuthUrl() {
        String authUrl = "https://kauth.kakao.com/oauth/authorize?client_id=" + clientId + "&redirect_uri=" + redirectUrl + "&response_type=code";
        return ResponseEntity.ok(authUrl);
    }

    @Value("${NAVER_CLIENT_ID}")
    private String naverClientId;

    @Value("${NAVER_REDIRECT_URL}")
    private String naverRedirectUrl;

    @GetMapping("/naver-url")
    @Operation(summary = "Naver-url", description = "OAuth 인증에 필요한 각 유저의 Naver-url 출력 API입니다.")
    public ResponseEntity<String> getNaverAuthUrl() {
        log.info("네이버 Client ID: " + naverClientId);
        log.info("네이버 Redirect URL: " + naverRedirectUrl);

        String encodedRedirectUri = URLEncoder.encode(naverRedirectUrl, StandardCharsets.UTF_8);
        String state = URLEncoder.encode(generateState(), StandardCharsets.UTF_8);

        String authUrl = "https://nid.naver.com/oauth2.0/authorize?client_id=" + naverClientId +
                "&redirect_uri=" + encodedRedirectUri + "&response_type=code&state=" + state;

        return ResponseEntity.ok(authUrl);
    }

    private String generateState() {
        SecureRandom random = new SecureRandom();
        byte[] stateBytes = new byte[10];
        random.nextBytes(stateBytes);
        return Base64.getUrlEncoder().encodeToString(stateBytes);
    }
}
