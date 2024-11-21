package nbc_final.gathering.domain.payment.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentFailResponseDto {

    private String code;
    private String message;
    private String orderId;

    private PaymentFailResponseDto(String code, String message, String orderId) {
        this.code = code;
        this.message = message;
        this.orderId = orderId;
    }

    public static PaymentFailResponseDto of(String code, String message, String orderId) {
        return new PaymentFailResponseDto(code, message, orderId);
    }
}
