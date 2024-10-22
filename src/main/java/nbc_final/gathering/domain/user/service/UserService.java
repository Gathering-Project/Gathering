package nbc_final.gathering.domain.user.service;


import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc_final.gathering.common.config.JwtUtil;
import nbc_final.gathering.domain.user.dto.request.LoginRequestDto;
import nbc_final.gathering.domain.user.dto.request.SignupRequestDto;
import nbc_final.gathering.domain.user.dto.request.UserChangePwRequestDto;
import nbc_final.gathering.domain.user.dto.response.UserGetResponseDto;
import nbc_final.gathering.domain.user.dto.response.LoginResponseDto;
import nbc_final.gathering.domain.user.dto.response.SignUpResponseDto;
import nbc_final.gathering.domain.user.entity.User;
import nbc_final.gathering.domain.user.enums.UserRole;
import nbc_final.gathering.domain.user.repository.UserRepository;
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
            throw new IllegalArgumentException("이미 존재하거나 탈퇴한 이메일입니다.");
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
            throw new IllegalArgumentException("이미 탈퇴한 이메일입니다.");
        }

        User user = userRepository.findByEmail(requestDto.getEmail()).orElseThrow(
                () -> new IllegalArgumentException("가입되지 않은 유저입니다."));

        // 로그인 시 이메일과 비밀번호가 일치하지 않을 경우 401을 반환합니다.
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }

        String bearerToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole(), user.getNickname());
        jwtUtil.addJwtToCookie(bearerToken, response);

        return new LoginResponseDto(bearerToken);
    }

    @Transactional
    public void deleteUser(LoginRequestDto requestDto) {

        if (userRepository.findByEmailAndIsDeletedTrue(requestDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 탈퇴한 이메일입니다.");
        }
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 로그인 시 이메일과 비밀번호가 일치하지 않을 경우 401을 반환합니다.
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }

        user.updateIsDeleted(); // 회원 탈퇴
    }

    // 유저 정보 조회
    public UserGetResponseDto getUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow();

        return UserGetResponseDto.of(user);
    }

    // 유저 비밀번호 변경
    @Transactional
    public void changePassword(UserChangePwRequestDto requestDto, Long userId) {
        validateNewPassword(requestDto);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (passwordEncoder.matches(requestDto.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");
        }

        if (!passwordEncoder.matches(requestDto.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }

        log.info(requestDto.getNewPassword());
        user.changePassword(passwordEncoder.encode(requestDto.getNewPassword()));
    }

    // 새 비밀번호 검증
    private static void validateNewPassword(UserChangePwRequestDto requestDto) {
        if (
                // 비밀번호에 알파벳 포함 여부 확인 (대소문자 포함)
                !requestDto.getNewPassword().matches(".*[A-Za-z].*") ||
                // 비밀번호에 숫자 포함 여부 확인
                !requestDto.getNewPassword().matches(".*\\d.*") ||
                // 비밀번호에 특수 문자 포함 여부 확인
                !requestDto.getNewPassword().matches(".*[\\p{Punct}].*") ||
                // 비밀번호 길이가 8자 이상 20자 이하인지 확인
                requestDto.getNewPassword().length() < 8 ||
                requestDto.getNewPassword().length() > 20
        ) {
            throw new IllegalArgumentException("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.");
        }
    }


}






