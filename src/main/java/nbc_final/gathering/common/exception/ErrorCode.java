package nbc_final.gathering.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    EXAMPLE_EXCEPTION(HttpStatus.BAD_REQUEST.value(), "예시 예외입니당.");



    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
