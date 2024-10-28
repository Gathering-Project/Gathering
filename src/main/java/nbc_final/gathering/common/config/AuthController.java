package nbc_final.gathering.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Value("${kakao.client.id}")
    private String clientId; // 환경변수에서 클라이언트 ID 가져오기

    @Value("${kakao.redirect.url}")
    private String redirectUrl; // 환경변수에서 리다이렉트 URL 가져오기

    @GetMapping("/kakao-url")
    public ResponseEntity<String> getKakaoAuthUrl() {
        String authUrl = "https://kauth.kakao.com/oauth/authorize?client_id=" + clientId + "&redirect_uri=" + redirectUrl + "&response_type=code";
        return ResponseEntity.ok(authUrl);
    }
}
