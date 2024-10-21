package nbc_final.gathering.domain.user.repository;



import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc_final.gathering.common.config.JwtUtil;
import nbc_final.gathering.domain.user.dto.request.SignupRequestDto;
import nbc_final.gathering.domain.user.dto.response.SignUpResponseDto;
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

    @Transactional
    public SignUpResponseDto signup(SignupRequestDto signupRequest) {

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
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



//    public SigninResponse signin(SigninRequest signinRequest) {
//        User user = userRepository.findByEmail(signinRequest.getEmail()).orElseThrow(
//                () -> new InvalidRequestException("가입되지 않은 유저입니다."));
//
//        // 로그인 시 이메일과 비밀번호가 일치하지 않을 경우 401을 반환합니다.
//        if (!passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())) {
//            throw new AuthException("잘못된 비밀번호입니다.");
//        }
//
//        String bearerToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getNickname());
//
//        return new SigninResponse(bearerToken);
//    }




}
