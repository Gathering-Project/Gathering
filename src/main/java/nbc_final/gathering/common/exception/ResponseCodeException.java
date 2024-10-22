package nbc_final.gathering.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ResponseCodeException extends RuntimeException {
    private final HttpStatus httpStatus;

    public ResponseCodeException(ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.httpStatus = responseCode.getHttpStatus();
    }

    public ResponseCodeException(ResponseCode responseCode, String customMessage) {
        super(customMessage);
        this.httpStatus = responseCode.getHttpStatus();
    }
}
