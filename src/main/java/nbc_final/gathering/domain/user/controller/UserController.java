package nbc_final.gathering.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import nbc_final.gathering.common.config.jwt.JwtUtil;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ApiResponse;
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

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "User API", description = "사용자 관련 API 모음입니다.")
public class UserController {

    private final UserService userService;
    private final KakaoService kakaoService;
    private final NaverService naverService;

    /**
     * 유저 회원가입
     *
     * @param requestDto 생성 요청 데이터
     * @return 유저(회원) 생성
     */
    @Operation(summary = "유저 회원가입", description = "서비스에 회원가입합니다.(닉네임 미입력시 '엉뚱한 하츄핑 4536'같은 랜덤 닉네임 생성)")
    @Parameter(name = "email", description = "회원가입 계정 이메일")
    @Parameter(name = "nickname", description = "닉네임, Required = false || 미기입 시 '엉뚱한 하츄핑 4536'같은 랜덤 닉네임 생성)")
    @Parameter(name = "password", description = "비밀번호, 비밀번호는 8자 이상 20자 이하이어야 하며, 적어도 하나의 알파벳, 하나의 숫자, 하나의 특수 문자를 포함해야 합니다.")
    @Parameter(name = "userRole", description = "계정 권한, 일반 사용자: ROLE_USER / 관리자: ROLE_ADMIN")
    @Parameter(name = "adminToken", description = "관리자 확인 토큰, Required = false || ROLE_ADMIN 설정 시 관리 측 여부 확인")
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
    @Operation(summary = "유저 로그인", description = "서비스에 로그인 합니다.")
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
    @Operation(summary = "유저 정보 단건 조회", description = "해당 유저의 정보를 조회합니다.")
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
    @Operation(summary = "회원 탈퇴", description = "회원에서 탈퇴합니다. 한 번 탈퇴한 이메일 계정은 다시 사용하실 수 없습니다.")
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
    @Operation(summary = "비밀번호 변경", description = "비밀번호를 변경합니다.")
    @Parameter(name = "newPassword", description = "변경하고 싶은 비밀번호를 입력합니다. 이전 비밀번호와 동일하지 않고 8자 이상 20자 이하이어야 하며, 적어도 하나의 알파벳, 하나의 숫자, 하나의 특수 문자를 포함해야 합니다.")
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
    @Operation(summary = "유저 정보 수정", description = "유저의 개인 정보를 수정합니다.")
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
    @Operation(summary = "Kakao 로그인", description = "카카오 계정을 통해 간편하게 로그인합니다.")
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
    @Operation(summary = "Kakao 설정 정보 가져오기", description = "카카오 API 설정 정보들을 가져옵니다.")
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
    @Operation(summary = "Naver 로그인", description = "네이버 계정을 통해 간편하게 로그인합니다.")
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
