package nbc_final.gathering.domain.user.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.config.JwtUtil;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ApiResponse;
import nbc_final.gathering.domain.user.dto.UserElasticDto;
import nbc_final.gathering.domain.user.dto.request.*;
import nbc_final.gathering.domain.user.dto.response.LoginResponseDto;
import nbc_final.gathering.domain.user.dto.response.SignUpResponseDto;
import nbc_final.gathering.domain.user.dto.response.UserGetResponseDto;
import nbc_final.gathering.domain.user.service.KakaoService;
import nbc_final.gathering.domain.user.service.NaverService;
import nbc_final.gathering.domain.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final KakaoService kakaoService;
    private final JwtUtil jwtUtil;
    private final NaverService naverService;

    /**
     * 유저 검색
     * @param keyword 검색 키워드
     * @return 검색된 유저 리스트
     */
    @GetMapping("users/search")
    public ResponseEntity<ApiResponse<List<UserElasticDto>>> searchUsers(@RequestParam String keyword) {
        List<UserElasticDto> users = userService.searchUsers(keyword);
        return ResponseEntity.ok(ApiResponse.createSuccess(users));
    }

    /**
     * 유저 회원가입
     *
     * @param requestDto 생성 요청 데이터
     * @return 유저(회원) 생성
     */
    @PostMapping("/v1/users/signup")
    public ResponseEntity<ApiResponse<SignUpResponseDto>> signup(@RequestBody @Valid SignupRequestDto requestDto) {
        SignUpResponseDto res = userService.signup(requestDto);
        return ResponseEntity.ok(ApiResponse.createSuccess(res));
    }

    /**
     * 유저 로그인
     *
     * @param requestDto 생성 요청 데이터
     * @param response   http 응답
     * @return 유저 로그인
     */
    @PostMapping("/v1/users/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@RequestBody @Valid LoginRequestDto requestDto, HttpServletResponse response) {
        LoginResponseDto res = userService.login(requestDto, response);
        return ResponseEntity.ok(ApiResponse.createSuccess(res));
    }


    /**
     * 유저 조회
     *
     * @param userId 유저 ID
     * @return 조회된 유저 정보
     */
    @GetMapping("/v1/users/{userId}")
    public ResponseEntity<ApiResponse<UserGetResponseDto>> getUser(@PathVariable Long userId) {
        UserGetResponseDto res = userService.getUser(userId);
        return ResponseEntity.ok(ApiResponse.createSuccess(res));
    }

    /**
     * 유저 회원 탈퇴
     * <p>
     * //     * @param authUser 인증 사용자
     *
     * @param requestDto 생성 요청 데이터
     * @return 성공 여부
     */
    @DeleteMapping("/v1/users")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody @Valid UserDeleteRequestDto requestDto
    ) {
        userService.deleteUser(authUser.getUserId(), requestDto);
        return ResponseEntity.ok(ApiResponse.createSuccess(null));
    }

    /**
     * 유저 비밀번호 변경
     *
     * @param authUser   인증 사용자
     * @param requestDto 생성 요청 데이터
     * @return 성공 여부
     */
    @PutMapping("/v1/users/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@AuthenticationPrincipal AuthUser authUser, @RequestBody @Valid UserChangePwRequestDto requestDto) {
        userService.changePassword(requestDto, authUser.getUserId());
        return ResponseEntity.ok(ApiResponse.createSuccess(null));
    }

    /**
     * 유저 정보 수정
     *
     * @param authUser   인증 사용자
     * @param requestDto 생성 요청 데이터
     * @return 수정된 유저 상세 정보
     */
    @PutMapping("/v1/users")
    public ResponseEntity<ApiResponse<UserGetResponseDto>> updateMyInfo(@AuthenticationPrincipal AuthUser authUser, @RequestBody UserUpdateRequestDto requestDto) {
        UserGetResponseDto res = userService.updateInfo(authUser.getUserId(), requestDto);
        return ResponseEntity.ok(ApiResponse.createSuccess(res));
    }

    //----------------- OAuth 2.0  ------------- //

    /**
     * 카카오 로그인
     *
     * @param code     카카오 인가 코드
     * @param response HTTP 응답 객체 (JWT 토큰을 쿠키에 저장)
     * @return 로그인 결과 (JWT 토큰 및 유저 정보)
     */
    @GetMapping("/v1/users/kakao/callback")
    public ResponseEntity<ApiResponse<LoginResponseDto>> kakaoLogin(@RequestParam String code, HttpServletResponse response) {
        LoginResponseDto loginResponse = kakaoService.kakaoLogin(code, response);
        return ResponseEntity.ok(ApiResponse.createSuccess(loginResponse));
    }

    /**
     * 카카오 설정 정보 가져오기
     *
     * @return 카카오 API 설정 정보 (클라이언트 ID, 리다이렉트 URL 등)
     */
    @GetMapping("/v1/users/kakao/config")
    public ResponseEntity<ApiResponse<Map<String, String>>> getKakaoConfig() {
        Map<String, String> config = kakaoService.getKakaoConfig();
        return ResponseEntity.ok(ApiResponse.createSuccess(config));
    }

    /**
     * 네이버 로그인 엔드포인트
     *
     *
     * @param code     네이버에서 전달받은 인증 코드
     * @param state    네이버에서 전달받은 state 값 (CSRF 방지용)
     * @param response HTTP 응답 객체 (JWT 토큰을 쿠키에 저장)
     * @return 로그인 결과로 JWT 토큰과 유저 정보를 포함하는 ApiResponse 객체
     */
    @GetMapping("/v1/users/naver/callback")
    public ResponseEntity<ApiResponse<LoginResponseDto>> naverCallback(
            @RequestParam String code,
            @RequestParam String state,
            HttpServletResponse response
    ) {
        LoginResponseDto loginResponse = naverService.naverLogin(code, state, response);
        return ResponseEntity.ok(ApiResponse.createSuccess(loginResponse));
    }
}
