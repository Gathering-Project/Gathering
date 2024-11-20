package nbc_final.gathering.domain.payment.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentCancelRequestDto {
    private String paymentKey;
    private String cancelReason;

    private PaymentCancelRequestDto(String paymentKey, String cancelReason) {
        this.paymentKey = paymentKey;
        this.cancelReason = cancelReason;
    }

    public static PaymentCancelRequestDto of(String paymentKey, String cancelReason) {
        return new PaymentCancelRequestDto(paymentKey, cancelReason);
    }
}
