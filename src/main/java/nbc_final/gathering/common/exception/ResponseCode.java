package nbc_final.gathering.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ResponseCode {

    // 공통 응답
    SUCCESS(HttpStatus.OK, "정상 처리되었습니다."),
    INVALID_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "다시 시도해주세요."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // 유저 관련 예외
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "해당 사용자는 존재하지 않습니다."),
    WRONG_EMAIL_OR_PASSWORD(HttpStatus.BAD_REQUEST, "이메일 또는 비밀번호가 일치하지 않습니다."),
    NOT_FOUND_EMAIL(HttpStatus.NOT_FOUND, "해당 이메일을 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "잘못된 비밀번호입니다."),
    USER_ALREADY_DELETED(HttpStatus.GONE, "이미 탈퇴한 회원입니다."),

    // 멤버 관련 예외
    NOT_FOUND_MEMBER(HttpStatus.NOT_FOUND, "해당 멤버를 찾을 수 없습니다."),

    // 소모임 관련 예외
    NOT_FOUND_GROUP(HttpStatus.NOT_FOUND, "해당 소모임을 찾을 수 없습니다."),

    // 이벤트 관련 예외
    NOT_FOUND_EVENT(HttpStatus.NOT_FOUND, "해당 이벤트를 찾을 수 없습니다."),

    // 댓글 관련 예외
    NOT_FOUND_COMMENT(HttpStatus.NOT_FOUND, "해당 댓글을 찾을 수 없습니다."),

    // 첨부파일 관련 예외
    NOT_FOUND_FILE(HttpStatus.NOT_FOUND, "해당 첨부파일을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
