package nbc_final.gathering.domain.user.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc_final.gathering.common.config.common.WebSocketSessionManager;
import nbc_final.gathering.common.config.jwt.JwtUtil;
import nbc_final.gathering.common.exception.ResponseCode;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.common.kafka.util.KafkaNotificationUtil;
import nbc_final.gathering.domain.user.dto.request.*;
import nbc_final.gathering.domain.user.dto.response.LoginResponseDto;
import nbc_final.gathering.domain.user.dto.response.SignUpResponseDto;
import nbc_final.gathering.domain.user.dto.response.UserGetResponseDto;
import nbc_final.gathering.domain.user.entity.User;
import nbc_final.gathering.domain.user.enums.UserRole;
import nbc_final.gathering.domain.user.repository.UserRepository;
import nbc_final.gathering.domain.user.utils.GenerateRandomNickname;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KakaoService kakaoService;
    private final NaverService naverService;
    private final KafkaNotificationUtil kafkaNotificationUtil;
    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketSessionManager webSocketSessionManager;
    private final JwtUtil jwtUtil;

    @Value("${ADMIN_TOKEN}")
    private String ADMIN_TOKEN; // 관리자가 맞는지 확인 토큰

    @PersistenceContext // EntityManager 주입
    private EntityManager entityManager;


    // 유저 회원가입
    @Transactional
    public SignUpResponseDto signup(SignupRequestDto signupRequest) {

        // 이미 있는 이메일인지 확인
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new ResponseCodeException(ResponseCode.DUPLICATE_EMAIL);
        }

        // 이미 있는 닉네임인지 확인
        if (userRepository.existsByNickname(signupRequest.getNickname()) && signupRequest.getNickname() != null) {
            throw new ResponseCodeException(ResponseCode.DUPLICATE_NICKNAME);
        }

        String email = signupRequest.getEmail();
        validateDeletedEmail(email); // 이미 탈퇴한 적 있는 이메일인지 확인
        validateAdminToken(signupRequest, signupRequest.getUserRole()); // 관리자 계정 생성 시 관리자 인증 토큰이 맞는지 확인

        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

        UserRole userRole = UserRole.of(signupRequest.getUserRole());

        // 유저(회원) 생성
        User newUser = User.builder()
                .nickname(signupRequest.getNickname())
                .email(signupRequest.getEmail())
                .password(encodedPassword)
                .userRole(userRole)
                .build();

        while (newUser.getNickname() == null) {
            try {
                String randomNickname = GenerateRandomNickname.generateNickname();
                if (userRepository.existsByNickname(randomNickname)) {
                    throw new ResponseCodeException(ResponseCode.DUPLICATE_NICKNAME);
                }
                newUser.setRandomNickname(randomNickname);
            } catch (ResponseCodeException e) {
                log.info("중복되는 닉네임이 생성되어 닉네임을 재부여합니다");
            }
        }

        User savedUser = userRepository.save(newUser); //회원 저장
        String bearerToken = jwtUtil.createToken(savedUser.getId(), savedUser.getEmail(), savedUser.getUserRole(), savedUser.getNickname());

        return new SignUpResponseDto(bearerToken); // 토큰 반환
    }

    // 유저 로그인
    public LoginResponseDto login(LoginRequestDto requestDto, HttpServletResponse response) {

        String email = requestDto.getEmail();
        validateDeletedEmail(email); // 이미 탈퇴한 적 있는 이메일인지 확인

        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));

        String inputPassword = requestDto.getPassword();
        String correctPassword = user.getPassword();
        validateCorrectPassword(inputPassword, correctPassword); // 비밀번호 맞는지 검증

        String bearerToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole(), user.getNickname());
        jwtUtil.addJwtToCookie(bearerToken, response);

        kafkaNotificationUtil.notifyUser(user.getId(), "로그인에 성공했습니다.");

        // WebSocket 세션 ID 생성 및 Redis 저장
        String websocketSessionId = generateWebSocketSessionId(user.getId());
        webSocketSessionManager.addUserSession(user.getId(), websocketSessionId);

        // 클라이언트가 WebSocket 연결을 수행할 수 있는 URL 제공
        String websocketUrl = "ws://localhost:9090/gathering/inbox?token=" + bearerToken;


        return new LoginResponseDto(bearerToken, websocketUrl);
    }

    // 유저 회원 탈퇴
    @Transactional
    public void deleteUser(Long userId, UserDeleteRequestDto requestDto) {
        User user = getUserById(userId);

        try {
            if (user.getKakaoId() != null) {
                kakaoService.unlinkKakaoAccount(getAccessTokenForUser(user));
            } else if (user.getNaverId() != null) {
                try {
                    naverService.expireNaverAccessToken(getAccessTokenForUser(user));
                    log.info("Naver access token expired successfully for userId: {}", userId);
                } catch (ResponseCodeException e) {
                    log.warn("네이버 토큰 만료 실패, 예외 무시하고 회원 탈퇴 계속 진행");
                }
            } else {
                validateCorrectPassword(requestDto.getPassword(), user.getPassword());
            }
            user.updateIsDeleted();
            log.info("회원 탈퇴 처리 완료 및 DB 반영됨, userId: {}", userId);
        } catch (Exception e) {
            log.error("회원 탈퇴 처리 중 오류 발생, userId: {}: {}", userId, e.getMessage());
            throw new ResponseCodeException(ResponseCode.UNLINK_FAILED);
        }
    }
    // 유저 정보 조회
    public UserGetResponseDto getUser(Long userId) {
        User user = getUserById(userId);
        return UserGetResponseDto.of(user);
    }

    // 유저 비밀번호 변경
    @Transactional
    public void changePassword(UserChangePwRequestDto requestDto, Long userId) {

        User user = getUserById(userId);
        // SNS 로그인 유저는 권한 없음 예외 발생
        if (user.getKakaoId() != null || user.getNaverId() != null) {
            throw new ResponseCodeException(ResponseCode.NO_PERMISSION_CHANGE_PASSWORD);
        }

        validateNewPassword(requestDto); // 새로 변경하려는 비밀번호가 규칙에 맞는지 검증

        String inputOldPassword = requestDto.getOldPassword();
        String correctPassword = user.getPassword();
        validateCorrectPassword(inputOldPassword, correctPassword);

        if (passwordEncoder.matches(requestDto.getNewPassword(), user.getPassword())) {
            throw new ResponseCodeException(ResponseCode.SAME_PASSWORD);
        }

        log.info(requestDto.getNewPassword());
        user.changePassword(passwordEncoder.encode(requestDto.getNewPassword()));

        // 비밀번호 변경 알림 전송
        kafkaNotificationUtil.notifyUser(userId, "비밀번호가 변경되었습니다.");
    }

    // 내 정보 업데이트(수정)
    @Transactional
    public UserGetResponseDto updateInfo(Long userId, UserUpdateRequestDto requestDto) {
        User user = getUserById(userId);
        user.updateInfo(requestDto); // 유저 정보 갱신

        kafkaNotificationUtil.notifyUser(userId, "내 정보가 수정되었습니다.");

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
            // 비밀번호는 영문 + 숫자 + 특수문자를 최소 1글자 포함하고 최소 8글자 이상 최대 20글자 이하
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
            throw new ResponseCodeException(ResponseCode.VIOLATION_PASSWORD);
        }
    }

    // 관리자 계정 생성 인증
    public void validateAdminToken(SignupRequestDto requestDto, String userRole) {
        if (requestDto.getUserRole().equals("ROLE_ADMIN") && !(ADMIN_TOKEN.equals(requestDto.getAdminToken()))) {
            throw  new ResponseCodeException(ResponseCode.INVALID_ADMIN_TOKEN);
        }
    }

    private String getAccessTokenForUser(User user) {
        return "access-token"; // 실제 액세스 토큰 반환으로 변경 필요
    }

    private String generateWebSocketSessionId(Long userId) {
        return "ws-session-" + userId;
    }

}