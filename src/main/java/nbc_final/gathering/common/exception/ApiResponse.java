package nbc_final.gathering.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiResponse<T> {
    private int statusCode;
    private String message;
    private T data;

    // 성공 응답 생성
    public static <T> ApiResponse<T> createSuccess(T data) {
        return new ApiResponse<>(HttpStatus.OK.value(), "정상 처리되었습니다.", data);
    }

    // 에러 응답 생성 (상태 코드와 메시지를 함께 반환)
    public static ApiResponse<Void> createError(int statusCode, String message) {
        return new ApiResponse<>(statusCode, message, null);
    }

    // 상태 코드, 메시지, 데이터를 관리
    private ApiResponse(int statusCode, String message, T data) {
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
    }
}
