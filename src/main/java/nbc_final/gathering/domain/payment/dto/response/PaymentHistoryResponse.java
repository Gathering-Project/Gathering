package nbc_final.gathering.domain.payment.dto.response;

import lombok.Getter;
import nbc_final.gathering.domain.payment.entity.Payment;

import java.time.LocalDateTime;

@Getter
public class PaymentHistoryResponse {
    private final Long paymentHistoryId;  // 결제 기록 ID
    private final Long amount;            // 결제 금액
    private final String orderName;       // 주문명
    private final boolean isPaySuccess;   // 결제 성공 여부
    private final LocalDateTime createdAt; // 결제 시간

    private PaymentHistoryResponse(Long paymentHistoryId, Long amount, String orderName, boolean isPaySuccess, LocalDateTime createdAt) {
        this.paymentHistoryId = paymentHistoryId;
        this.amount = amount;
        this.orderName = orderName;
        this.isPaySuccess = isPaySuccess;
        this.createdAt = createdAt;
    }

    public static PaymentHistoryResponse from(Payment payment) {
        return new PaymentHistoryResponse(
                payment.getPaymentId(),
                payment.getAmount(),
                payment.getOrderName(),
                payment.isPaySuccess(),
                payment.getCreatedAt()
        );
    }
}
