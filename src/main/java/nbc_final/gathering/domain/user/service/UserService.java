package nbc_final.gathering.domain.user.service;


import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc_final.gathering.common.config.JwtUtil;
import nbc_final.gathering.domain.user.dto.request.*;
import nbc_final.gathering.common.exception.ResponseCode;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.domain.user.dto.request.LoginRequestDto;
import nbc_final.gathering.domain.user.dto.request.SignupRequestDto;
import nbc_final.gathering.domain.user.dto.response.LoginResponseDto;
import nbc_final.gathering.domain.user.dto.response.SignUpResponseDto;
import nbc_final.gathering.domain.user.dto.response.UserGetResponseDto;
import nbc_final.gathering.domain.user.entity.User;
import nbc_final.gathering.domain.user.enums.UserRole;
import nbc_final.gathering.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${admin.token}")
    private String ADMIN_TOKEN;


    // 유저 회원가입
    @Transactional
    public SignUpResponseDto signup(SignupRequestDto signupRequest) {

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new ResponseCodeException(ResponseCode.DUPLICATE_EMAIL);
        }

        String email = signupRequest.getEmail();
        validateDeletedEmail(email); // 이미 탈퇴한 적 있는 이메일인지 확인
        validateAdminToken(signupRequest, signupRequest.getUserRole()); // 관리자 계정 생성 시 관리자 인증 토큰이 맞는지 확인

        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

        UserRole userRole = UserRole.of(signupRequest.getUserRole());

        User newUser = User.builder()
                .nickname(signupRequest.getNickname())
                .email(signupRequest.getEmail())
                .password(encodedPassword)
                .userRole(userRole)
                .build();

        User savedUser = userRepository.save(newUser);

        String bearerToken = jwtUtil.createToken(savedUser.getId(), savedUser.getEmail(), savedUser.getUserRole(), savedUser.getNickname());

        return new SignUpResponseDto(bearerToken);
    }

    // 유저 로그인
    public LoginResponseDto login(LoginRequestDto requestDto, HttpServletResponse response) {

        String email = requestDto.getEmail();
        validateDeletedEmail(email); // 이미 탈퇴한 적 있는 이메일인지 확인

        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));

        String inputPassword = requestDto.getPassword();
        String correctPassword = user.getPassword();
        validateCorrectPassword(inputPassword, correctPassword);

        String bearerToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole(), user.getNickname());
        jwtUtil.addJwtToCookie(bearerToken, response);

        return new LoginResponseDto(bearerToken);
    }

    // 유저 회원 탈퇴
    @Transactional
    public void deleteUser(Long userId, UserDeleteRequestDto requestDto) {

        User user = getUserById(userId);

        String inputPassword = requestDto.getPassword();
        String correctPassword = user.getPassword();
        validateCorrectPassword(inputPassword, correctPassword);

        user.updateIsDeleted(); // 회원 탈퇴
    }

    // 유저 정보 조회
    public UserGetResponseDto getUser(Long userId) {
        User user = getUserById(userId);
        return UserGetResponseDto.of(user);
    }

    // 유저 비밀번호 변경
    @Transactional
    public void changePassword(UserChangePwRequestDto requestDto, Long userId) {

        validateNewPassword(requestDto); // 새로 변경하려는 비밀번호가 규칙에 맞는지 검증
        User user = getUserById(userId);


        if (passwordEncoder.matches(requestDto.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");
        }

        String inputOldPassword = requestDto.getOldPassword();
        String correctPassword = user.getPassword();
        validateCorrectPassword(inputOldPassword, correctPassword);

        log.info(requestDto.getNewPassword());
        user.changePassword(passwordEncoder.encode(requestDto.getNewPassword()));
    }

    // 내 정보 업데이트(수정)
    @Transactional
    public UserGetResponseDto updateInfo(Long userId, UserUpdateRequestDto requestDto) {
        User user = getUserById(userId);
        user.updateInfo(requestDto); // 유저 정보 갱신

        return UserGetResponseDto.of(user);
    }

    // 내 정보 조회
    public UserGetResponseDto getMyInfo(Long myId) {
        User user = getUserById(myId);

        return UserGetResponseDto.of(user);

    }

    //----------------- extracted method ------------- //

    private User getUserById(Long myId) {
        User user = userRepository.findById(myId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));
        return user;
    }

    // 비밀번호 검증
    private void validateCorrectPassword(String inputPassword, String correctPassword) {
        if (!passwordEncoder.matches(inputPassword, correctPassword)) {
            throw new ResponseCodeException(ResponseCode.INVALID_PASSWORD);
        }
    }

    // 이미 탈퇴한 적 있는 이메일인지 검증
    private void validateDeletedEmail(String email) {
        if (userRepository.findByEmailAndIsDeletedTrue(email).isPresent()) {
            throw new ResponseCodeException(ResponseCode.USER_ALREADY_DELETED);
        }
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
            throw new ResponseCodeException(ResponseCode.INVALID_PASSWORD);
        }
    }

    // 관리자 계정 생성 인증
    public void validateAdminToken(SignupRequestDto requestDto, String userRole) {
        if (requestDto.getUserRole().equals("ROLE_ADMIN") && !(ADMIN_TOKEN.equals(requestDto.getAdminToken()))) {
            throw  new ResponseCodeException(ResponseCode.INVALID_ADMIN_TOKEN);
        }
    }


}






