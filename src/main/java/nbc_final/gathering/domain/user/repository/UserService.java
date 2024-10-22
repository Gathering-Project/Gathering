package nbc_final.gathering.domain.user.repository;


import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc_final.gathering.common.config.JwtUtil;
import nbc_final.gathering.common.exception.ResponseCode;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.domain.user.dto.request.LoginRequestDto;
import nbc_final.gathering.domain.user.dto.request.SignupRequestDto;
import nbc_final.gathering.domain.user.dto.response.LoginResponseDto;
import nbc_final.gathering.domain.user.dto.response.SignUpResponseDto;
import nbc_final.gathering.domain.user.dto.response.UserGetResponseDto;
import nbc_final.gathering.domain.user.entity.User;
import nbc_final.gathering.domain.user.enums.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    // 유저 회원가입
    @Transactional
    public SignUpResponseDto signup(SignupRequestDto signupRequest) {

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new ResponseCodeException(ResponseCode.DUPLICATE_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

        UserRole userRole = UserRole.of(signupRequest.getUserRole());

        User newUser = User.builder()
                .location(signupRequest.getLocation())
                .nickname(signupRequest.getNickname())
                .email(signupRequest.getEmail())
                .password(encodedPassword)
                .interestType(signupRequest.getInterestType())
                .mbtiType(signupRequest.getMbtiType())
                .userRole(userRole)
                .build();

        User savedUser = userRepository.save(newUser);

        String bearerToken = jwtUtil.createToken(savedUser.getId(), savedUser.getEmail(), savedUser.getUserRole(), savedUser.getNickname());

        return new SignUpResponseDto(bearerToken);
    }

    // 유저 로그인
    public LoginResponseDto login(LoginRequestDto requestDto, HttpServletResponse response) {

        if (userRepository.findByEmailAndIsDeletedTrue(requestDto.getEmail()).isPresent()) {
            throw new ResponseCodeException(ResponseCode.USER_ALREADY_DELETED);
        }

        User user = userRepository.findByEmail(requestDto.getEmail()).orElseThrow(
                () -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));

        // 로그인 시 이메일과 비밀번호가 일치하지 않을 경우 401을 반환합니다.
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new ResponseCodeException(ResponseCode.WRONG_EMAIL_OR_PASSWORD);
        }

        String bearerToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole(), user.getNickname());
        jwtUtil.addJwtToCookie(bearerToken, response);

        return new LoginResponseDto(bearerToken);
    }

    @Transactional
    public void deleteUser(LoginRequestDto requestDto) {

        if (userRepository.findByEmailAndIsDeletedTrue(requestDto.getEmail()).isPresent()) {
            throw new ResponseCodeException(ResponseCode.USER_ALREADY_DELETED);
        }
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EMAIL));

        // 로그인 시 이메일과 비밀번호가 일치하지 않을 경우 401을 반환합니다.
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new ResponseCodeException(ResponseCode.INVALID_PASSWORD);
        }

        user.updateIsDeleted(); // 회원 탈퇴
    }

    // 유저 정보 조회
    public UserGetResponseDto getUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow();

        return UserGetResponseDto.of(user);
    }





}
