package nbc_final.gathering.domain.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailResponseDto {
    private String code;
    private String message;
    private String orderId;

    public static PaymentFailResponseDto of(String code, String message, String orderId) {
        return new PaymentFailResponseDto(code, message, orderId);
    }
}
