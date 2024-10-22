package nbc_final.gathering.domain.user.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ApiResponse;
import nbc_final.gathering.domain.user.dto.request.*;
import nbc_final.gathering.domain.user.dto.response.LoginResponseDto;
import nbc_final.gathering.domain.user.dto.response.SignUpResponseDto;
import nbc_final.gathering.domain.user.dto.response.UserGetResponseDto;
import nbc_final.gathering.domain.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    
    /**
     * 유저 회원가입
     *
     * @param requestDto
     * @return
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponseDto>> signup(@RequestBody @Valid SignupRequestDto requestDto) {
        SignUpResponseDto res = userService.signup(requestDto);
        return ResponseEntity.ok(ApiResponse.createSuccess(res));
    }

    /**
     * 유저 로그인
     *
     * @param requestDto
     * @param response
     * @return
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@RequestBody @Valid LoginRequestDto requestDto, HttpServletResponse response) {
        LoginResponseDto res = userService.login(requestDto, response);
        return ResponseEntity.ok(ApiResponse.createSuccess(res));
    }


    /**유저 조회
     *
     * @param userId
     * @return
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserGetResponseDto>> getUser(@PathVariable Long userId) {
        UserGetResponseDto res = userService.getUser(userId);
        return ResponseEntity.ok(ApiResponse.createSuccess(res));
    }

    /**
     * 유저 회원 탈퇴
     *
     * @param authUser
     * @param requestDto
     * @return
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteUser(@AuthenticationPrincipal AuthUser authUser, @RequestBody @Valid UserDeleteRequestDto requestDto) {
        userService.deleteUser(authUser.getUserId(), requestDto);
        return ResponseEntity.ok(ApiResponse.createSuccess(null));
    }

    /**
     * 유저 비밀번호 변경
     *
     * @param authUser
     * @param requestDto
     * @return
     */
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@AuthenticationPrincipal AuthUser authUser, @RequestBody @Valid UserChangePwRequestDto requestDto) {
        userService.changePassword(requestDto, authUser.getUserId());
        return ResponseEntity.ok(ApiResponse.createSuccess(null));
    }

    /**
     * 유저 정보 수정
     *
     * @param authUser
     * @param requestDto
     * @return
     */
    @PutMapping
    public ResponseEntity<ApiResponse<UserGetResponseDto>> updateMyInfo(@AuthenticationPrincipal AuthUser authUser, @RequestBody UserUpdateRequestDto requestDto) {
        UserGetResponseDto res = userService.updateInfo(authUser.getUserId(), requestDto);
        return ResponseEntity.ok(ApiResponse.createSuccess(res));
    }
}