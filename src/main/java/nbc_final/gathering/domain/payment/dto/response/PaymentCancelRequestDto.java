package nbc_final.gathering.domain.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentCancelRequestDto {
    private String paymentKey;
    private String cancelReason;
}
