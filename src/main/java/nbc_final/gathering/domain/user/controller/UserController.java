package nbc_final.gathering.domain.user.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.exception.ApiResponse;
import nbc_final.gathering.domain.user.dto.request.LoginRequestDto;
import nbc_final.gathering.domain.user.dto.request.SignupRequestDto;
import nbc_final.gathering.domain.user.dto.request.UserGetRequestDto;
import nbc_final.gathering.domain.user.dto.response.LoginResponseDto;
import nbc_final.gathering.domain.user.dto.response.SignUpResponseDto;
import nbc_final.gathering.domain.user.dto.response.UserGetResponseDto;
import nbc_final.gathering.domain.user.repository.UserService;
import org.springframework.http.ResponseEntity;
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
        UserGetResponseDto userResponse = userService.getUser(requestDto.getEmail());
        return ResponseEntity.ok(ApiResponse.createSuccess(userResponse));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteUser(@RequestBody @Valid LoginRequestDto requestDto) {
        userService.deleteUser(requestDto);
        return ResponseEntity.ok(ApiResponse.createSuccess(null));
    }

}
