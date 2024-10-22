package nbc_final.gathering.domain.user.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.response.ApiResponse;
import nbc_final.gathering.domain.user.dto.request.*;
import nbc_final.gathering.domain.user.dto.response.UserGetResponseDto;
import nbc_final.gathering.domain.user.dto.response.LoginResponseDto;
import nbc_final.gathering.domain.user.dto.response.SignUpResponseDto;
import nbc_final.gathering.domain.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;


    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponseDto>> signup(@RequestBody @Valid SignupRequestDto requestDto) {
        SignUpResponseDto res = userService.signup(requestDto);
        return ResponseEntity.ok(ApiResponse.createSuccess(res));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@RequestBody @Valid LoginRequestDto requestDto, HttpServletResponse response) {
        LoginResponseDto res = userService.login(requestDto, response);
        return ResponseEntity.ok(ApiResponse.createSuccess(res));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<UserGetResponseDto>> getUser(@RequestBody @Valid UserGetRequestDto requestDto) {
        UserGetResponseDto res = userService.getUser(requestDto.getEmail());
        return ResponseEntity.ok(ApiResponse.createSuccess(res));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteUser(@AuthenticationPrincipal AuthUser authUser, @RequestBody @Valid UserDeleteRequestDto requestDto) {
        userService.deleteUser(authUser.getUserId(), requestDto);
        return ResponseEntity.ok(ApiResponse.createSuccess(null));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@AuthenticationPrincipal AuthUser authUser, @RequestBody @Valid UserChangePwRequestDto requestDto) {
        userService.changePassword(requestDto, authUser.getUserId());
        return ResponseEntity.ok(ApiResponse.createSuccess(null));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<UserGetResponseDto>> updateMyInfo(@AuthenticationPrincipal AuthUser authUser, @RequestBody UserUpdateRequestDto requestDto) {
        UserGetResponseDto res = userService.updateInfo(authUser.getUserId(), requestDto);
        return ResponseEntity.ok(ApiResponse.createSuccess(res));
    }





}
