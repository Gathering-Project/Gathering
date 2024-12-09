package nbc_final.gathering.domain.user.service;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import nbc_final.gathering.common.exception.ResponseCode;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.domain.user.dto.request.*;
import nbc_final.gathering.domain.user.dto.response.UserGetResponseDto;
import nbc_final.gathering.domain.user.entity.User;
import nbc_final.gathering.domain.user.enums.InterestType;
import nbc_final.gathering.domain.user.enums.MbtiType;
import nbc_final.gathering.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Mock
    HttpServletResponse httpServletResponse;

    private static final String USERNAME = "root";
    private static final String PASSWORD = "1234";
    private static final String DATABASE_NAME = "gathering_test";

    @Container
    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.0")
            .withUsername(USERNAME)
            .withPassword(PASSWORD)
            .withDatabaseName(DATABASE_NAME)
            .withInitScript("testcontainers/user_data/init.sql");

    @DynamicPropertySource
    public static void overrideProps(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.datasource.url", () -> mySQLContainer.getJdbcUrl());
        dynamicPropertyRegistry.add("spring.datasource.username", () -> USERNAME);
        dynamicPropertyRegistry.add("spring.datasource.password", () -> PASSWORD);
//        dynamicPropertyRegistry.add("spring.jpa.hibernate.ddl-auto", () -> "create"); // init.sql이랑 동시에 쓰면 안 됨
    }

    @Test
    @Commit
    void init_sql_로_만든_DB_초기_테스트_데이터_확인() {
        // given
        Optional<User> userOptional = userRepository.findByEmail("test1@example.com");

        // when
        User user = userOptional.get();
        user.changePassword(passwordEncoder.encode(user.getPassword())); // 암호 인코딩
        System.out.println("유저 비밀번호:" + user.getPassword());
        userRepository.save(user);

        // then
        assertThat(userOptional).isPresent();
        assertThat(user.getEmail()).isEqualTo("test1@example.com");
        assertThat(user.isDeleted()).isEqualTo(false);
        assertThat(passwordEncoder.matches("123456789a!", user.getPassword())).isTrue();
    }

    @Nested
    class 회원가입_관련_테스트 {

        @Test
        void 일반_유저_회원가입에_성공한다() {
            // given
            SignupRequestDto requestDto = new SignupRequestDto(
                    "testNickname2",
                    "test2@example.com",
                    "123456789a!",
                    "ROLE_USER",
                    ""
            );

            // when
            userService.signup(requestDto);

            // then
            User savedUser = userRepository.findByEmail(requestDto.getEmail()).get();

            assertThat(savedUser.getNickname()).isEqualTo(requestDto.getNickname());
            assertThat(savedUser.getEmail()).isEqualTo(requestDto.getEmail());
            assertThat(savedUser.isDeleted()).isEqualTo(false);
        }

        @Test
        void 인증_토큰값을_이용하여_관리자_계정으로_회원가입에_성공한다() {
            // given
            SignupRequestDto requestDto = new SignupRequestDto(
                    "testNickname2",
                    "test2@example.com",
                    "123456789a!",
                    "ROLE_ADMIN",
                    "G3htVA9apWuax/eUwgVbgwc3wmbWGweggeqJXS7jaNw="
            );

            // when
            userService.signup(requestDto);

            // then
            User savedUser = userRepository.findByEmail(requestDto.getEmail()).get();

            assertThat(savedUser.getNickname()).isEqualTo(requestDto.getNickname());
            assertThat(savedUser.getEmail()).isEqualTo(requestDto.getEmail());
            assertThat(savedUser.isDeleted()).isEqualTo(false);
        }

        @Test
        void 회원가입시_이미_있는_이메일이면_DUPLICATE_EMAIL_에러가_발생한다() {
            // given
            Long userId = 1L;
            SignupRequestDto requestDto = new SignupRequestDto(
                    "testNickname2",
                    "test1@example.com", // 초기 데이터와 같은 이메일
                    "123456789a!",
                    "ROLE_USER",
                    ""
            );

            // when & then
            assertThatThrownBy(
                    () -> userService.signup(requestDto))
                    .isInstanceOf(ResponseCodeException.class)// 예외 타입 확인
                    .hasMessageContaining(ResponseCode.DUPLICATE_EMAIL.getMessage())  // 예외 메시지 확인
                    .extracting(e -> ((ResponseCodeException) e).getHttpStatus()); // 커스텀 코드 확인

        }

        @Test
        void 회원가입시_이미_있는_닉네임이면_DUPLICATE_NICKNAME_에러가_발생한다() {
            // given
            Long userId = 1L;
            SignupRequestDto requestDto = new SignupRequestDto(
                    "testNickname1", // 초기 데이터와 같은 닉네임
                    "test2@example.com",
                    "123456789a!",
                    "ROLE_USER",
                    ""
            );

            // when & then
            assertThatThrownBy(
                    () -> userService.signup(requestDto))
                    .isInstanceOf(ResponseCodeException.class)// 예외 타입 확인
                    .hasMessageContaining(ResponseCode.DUPLICATE_NICKNAME.getMessage());  // 예외 메시지 확인
        }

        @Test
        void 회원가입시_이미_탈퇴했던_이메일_계정이면_USER_ALREADY_DELETED_에러가_발생한다() {
            // given
            // 탈퇴시킬 유저
            User user = User.builder()
                    .email("alreadyDeletedEmail@com")
                    .build();

            SignupRequestDto requestDto = new SignupRequestDto(
                    "testNickname2",
                    "alreadyDeletedEmail@com",
                    "123456789a!",
                    "ROLE_USER",
                    ""
            );

            // when
            user.updateIsDeleted(); // 유저 탈퇴
            userRepository.save(user);

            // then
            assertThatThrownBy(
                    () -> userService.signup(requestDto))
                    .isInstanceOf(ResponseCodeException.class)// 예외 타입 확인
                    .hasMessageContaining(ResponseCode.USER_ALREADY_DELETED.getMessage());  // 예외 메시지 확인
        }

        @Test
        void 회원가입시_닉네임을_미기입하면_랜덤_닉네임이_생성되어_부여된다() {
            // given
            SignupRequestDto requestDto = new SignupRequestDto(
                    null,
                    "alreadyDeletedEmail@com",
                    "123456789a!",
                    "ROLE_USER",
                    ""
            );

            // when
            userService.signup(requestDto);
            User newUser = userRepository.findByEmail(requestDto.getEmail()).get();

            // then
            System.out.println(newUser.getNickname());
            assertThat(newUser.getNickname()).isNotEqualTo(requestDto.getNickname());
            assertThat(newUser.getNickname()).isNotNull();
        }
    }


    @Nested
    class 로그인_관련_테스트 {

        @Test
        void 유저가_로그인에_성공한다() {
            // given
            LoginRequestDto requestDto = new LoginRequestDto("test1@example.com", "123456789a!");
            User user = userRepository.findByEmail(requestDto.getEmail()).get();
            user.changePassword(passwordEncoder.encode(requestDto.getPassword()));

            // when
            userService.login(requestDto, httpServletResponse);

            assertThat(user).isNotNull();
            assertThat(user.isDeleted()).isFalse();
            assertThat(user.getEmail()).isEqualTo(requestDto.getEmail());
            assertThat(passwordEncoder.matches(requestDto.getPassword(), user.getPassword())).isTrue();
        }
    }


    @Nested
    class 유저_조회_관련_테스트 {

        @Test
        void 회원가입된_기존_유저를_조회한다() {
            Long userId = 1L;
            UserGetResponseDto user = userService.getUser(userId);

            assertThat(user.getEmail()).isEqualTo("test1@example.com");
            assertThat(user.getUserId()).isEqualTo(1L);
        }
    }


    @Nested
    // 비밀번호는 영문 + 숫자 + 특수문자를 최소 1글자 포함하고 최소 8글자 이상 최대 20글자 이하
    class 회원_정보_및_비밀번호_수정_관련_테스트 {

        @Test
        void 회원이_본인_정보_수정에_성공한다() {
            // given
            Long userId = 1L;
            UserUpdateRequestDto requestDto = new UserUpdateRequestDto(
                    "서울시",
                    "깜찍한 하츄핑 1234",
                    InterestType.EXERCISE,
                    MbtiType.ENFJ
            );

            // when
            UserGetResponseDto responseDto = userService.updateInfo(userId, requestDto);

            // then
            assertThat(responseDto.getNickname()).isEqualTo(requestDto.getNickname());
            assertThat(responseDto.getLocation()).isEqualTo(requestDto.getLocation());
            assertThat(responseDto.getInterestType()).isEqualTo(requestDto.getInterestType());
            assertThat(responseDto.getMbtiType()).isEqualTo(requestDto.getMbtiType());
        }

        @Test
        void 유저가_비밀번호_변경에_성공한다() {
            // given
            Long userId = 1L;
            User user = userService.getUserById(userId);
            UserChangePwRequestDto requestDto = new UserChangePwRequestDto
                    (
                            "123456789a!",
                            "123456789a@"
                    );

            // when
            userService.changePassword(requestDto, userId);

            // then
            assertThat(user).isNotNull();
            assertThat(passwordEncoder.matches(requestDto.getNewPassword(), user.getPassword())).isTrue();
        }

        @Test
        void 새_비밀번호에_특수문자가_없으면_VIOLATION_PASSWORD_에러가_발생한다() {
            Long userId = 1L;
            UserChangePwRequestDto requestDto = new UserChangePwRequestDto
                    (
                            "123456789a!",
                            "123456789a" // 새 비밀번호에 특수문자 없음
                    );

            // when & then
            assertThatThrownBy(
                    () -> userService.changePassword(requestDto, userId))
                    .isInstanceOf(ResponseCodeException.class)// 예외 타입 확인
                    .hasMessageContaining(ResponseCode.VIOLATION_PASSWORD.getMessage());  // 예외 메시지 확인
        }

        @Test
        void 새_비밀번호에_영문자가_없으면_VIOLATION_PASSWORD_에러가_발생한다() {
            Long userId = 1L;
            UserChangePwRequestDto requestDto = new UserChangePwRequestDto
                    (
                            "123456789a!",
                            "123456789!" // 새 비밀번호에 영문자 없음
                    );

            // when & then
            assertThatThrownBy(
                    () -> userService.changePassword(requestDto, userId))
                    .isInstanceOf(ResponseCodeException.class)// 예외 타입 확인
                    .hasMessageContaining(ResponseCode.VIOLATION_PASSWORD.getMessage());  // 예외 메시지 확인
        }


        @Test
        void 새_비밀번호에_숫자가_없으면_VIOLATION_PASSWORD_에러가_발생한다() {
            Long userId = 1L;
            UserChangePwRequestDto requestDto = new UserChangePwRequestDto
                    (
                            "123456789a!",
                            "abcedfghi!" // 새 비밀번호에 숫자 없음
                    );

            // when & then
            assertThatThrownBy(
                    () -> userService.changePassword(requestDto, userId))
                    .isInstanceOf(ResponseCodeException.class)// 예외 타입 확인
                    .hasMessageContaining(ResponseCode.VIOLATION_PASSWORD.getMessage());  // 예외 메시지 확인
        }

        @Test
        void 새_비밀번호가_8글자_미만이면_VIOLATION_PASSWORD_에러가_발생한다() {
            Long userId = 1L;
            UserChangePwRequestDto requestDto = new UserChangePwRequestDto
                    (
                            "123456789a!",
                            "123a!" // 새 비밀번호가 8글자 미만
                    );

            // when & then
            assertThatThrownBy(
                    () -> userService.changePassword(requestDto, userId))
                    .isInstanceOf(ResponseCodeException.class)// 예외 타입 확인
                    .hasMessageContaining(ResponseCode.VIOLATION_PASSWORD.getMessage());  // 예외 메시지 확인
        }

        @Test
        void 새_비밀번호가_20글자_초과하면_VIOLATION_PASSWORD_에러가_발생한다() {
            Long userId = 1L;
            UserChangePwRequestDto requestDto = new UserChangePwRequestDto
                    (
                            "123456789a!",
                            "123456789abcdefghijklmn!@#$%%^" // 새 비밀번호가 20글자 초과
                    );

            // when & then
            assertThatThrownBy(
                    () -> userService.changePassword(requestDto, userId))
                    .isInstanceOf(ResponseCodeException.class)// 예외 타입 확인
                    .hasMessageContaining(ResponseCode.VIOLATION_PASSWORD.getMessage());  // 예외 메시지 확인
        }
    }


    @Nested
    class 회원_탈퇴_관련_테스트 {

        @Test
        void 회원가입한_유저가_회원_탈퇴에_성공한다() {

            // given
            Long userId = 1L;
            User user = userService.getUserById(userId);
            UserDeleteRequestDto requestDto = new UserDeleteRequestDto("123456789a!");

            // when
            userService.deleteUser(userId, requestDto);

            //then
            assertThat(user).isNotNull();
            assertThat(user.isDeleted()).isTrue();
        }

    }


    @Nested
    class 비밀번호_검증_테스트 {

        @Test
        void 입력한_비밀번호가_일치하면_비밀번호_검증에_성공한다() {
            //given
            String inputPassword = "123456789a!";
            Long userId = 1L;
            User user = userService.getUserById(userId);
            System.out.println("유저 raw 비밀번호:" + user.getPassword());
            String correctPassword = user.getPassword();

            // when & then
            assertThatCode(() -> userService.validateCorrectPassword(inputPassword, correctPassword))
                    .doesNotThrowAnyException();
        }

        @Test
        void 입력한_비밀번호가_일치하지_않으면_비밀번호_검증에_실패한다() {

            // given
            String inputPassword = "123456789a@";
            Long userId = 1L;
            User user = userService.getUserById(userId);
            String correctPassword = user.getPassword();

            // when & then
            assertThatThrownBy(
                    () -> userService.validateCorrectPassword(inputPassword, correctPassword))
                    .isInstanceOf(ResponseCodeException.class)// 예외 타입 확인
                    .hasMessageContaining(ResponseCode.INVALID_PASSWORD.getMessage());  // 예외 메시지 확인

        }
    }
}