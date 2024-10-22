package nbc_final.gathering.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.context.support.DefaultMessageSourceResolvable;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ResponseCodeException 처리
    @ExceptionHandler(ResponseCodeException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseCodeException(ResponseCodeException e) {
        return ResponseEntity.status(e.getHttpStatus())
                .body(ApiResponse.createError(e.getHttpStatus().value(), e.getMessage()));
    }

    // Validation 관련 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        List<String> fieldErrorList = exception.getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();
        String errorMessage = String.join(", ", fieldErrorList);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.createError(HttpStatus.BAD_REQUEST.value(), errorMessage));
    }

    // 그 외 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.createError(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
    }
}

