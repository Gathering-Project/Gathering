package nbc_final.gathering.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 400 error
    INTERNET_SERVER_EXCEPTION(HttpStatus.BAD_REQUEST.value(),"서버 에러가 발생했습니다."),

    EXAMPLE_EXCEPTION(HttpStatus.BAD_REQUEST.value(), "예시 예외입니당.");



    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
