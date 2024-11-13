package nbc_final.gathering.domain.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.domain.payment.entity.Payment;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistoryResponseDto {
    private Long paymentHistoryId;
    private Long amount;
    private String orderName;
    private boolean isPaySuccess;
    private LocalDateTime createdAt;

    public static PaymentHistoryResponseDto from(Payment payment) {
        return new PaymentHistoryResponseDto(
                payment.getPaymentId(),
                payment.getAmount(),
                payment.getOrderName(),
                payment.isPaySuccess(),
                payment.getCreatedAt()
        );
    }
}
