package nbc_final.gathering.domain.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.domain.payment.entity.PayStatus;
import nbc_final.gathering.domain.payment.entity.Payment;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class PaymentHistoryResponseDto {

    private Long paymentHistoryId;
    private String orderId;
    private String orderName;
    private Long amount;
    private PayStatus status;
    private String paymentKey;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private PaymentHistoryResponseDto(Long paymentHistoryId, String orderId, String orderName, Long amount, PayStatus status, String paymentKey, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.paymentHistoryId = paymentHistoryId;
        this.orderId = orderId;
        this.orderName = orderName;
        this.amount = amount;
        this.status = status;
        this.paymentKey = paymentKey;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PaymentHistoryResponseDto of(Long paymentHistoryId, String orderId, String orderName, Long amount, PayStatus status, String paymentKey, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new PaymentHistoryResponseDto(paymentHistoryId, orderId, orderName, amount, status, paymentKey, createdAt, updatedAt);
    }

    public static PaymentHistoryResponseDto from(Payment payment) {
        return new PaymentHistoryResponseDto(
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getOrderName(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getPaymentKey(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
