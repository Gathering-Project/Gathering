package nbc_final.gathering.common.exception;

import nbc_final.gathering.common.response.ApiResponse;
import nbc_final.gathering.domain.example.ExampleException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 예외 처리 예시
    @ExceptionHandler(ExampleException.class)
    public ResponseEntity<ApiResponse<?>> handleExampleException(ExampleException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.createError(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
    }

}
