package nbc_final.gathering.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public class ApiResponse<T> {

    private int statusCode;
    private String message;
    private T data;

    /**
     * 성공 응답 생성
     */
    public static <T> ApiResponse<T> createSuccess(T data) { // 요청 성공시 응답 메서드
        return new ApiResponse<>(HttpStatus.OK.value(), "요청이 정상처리되었습니다.", data);
    }

    /**
     * 에러 응답 생성
     */
    public static <T> ApiResponse<T> createError(Integer statusCode, String message) { // 요청 실패시 응답 메서드
        return new ApiResponse<>(statusCode, message, null);
    }

}