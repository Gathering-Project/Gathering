package nbc_final.gathering.domain.payment.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.domain.payment.entity.Payment;

@Getter
@NoArgsConstructor
public class PaymentSuccessResponseDto {
    private Long amount;
    private String orderName;
    private String orderId;
    private String paymentKey;

    // 기존 생성자
    public PaymentSuccessResponseDto(Long amount, String orderName, String orderId, String paymentKey) {
        this.amount = amount;
        this.orderName = orderName;
        this.orderId = orderId;
        this.paymentKey = paymentKey;
    }

    // 필요한 필드만 포함하는 새 생성자 추가
    public PaymentSuccessResponseDto(Long amount, String orderId, String paymentKey) {
        this.amount = amount;
        this.orderId = orderId;
        this.paymentKey = paymentKey;
    }

    public static PaymentSuccessResponseDto from(Payment payment) {
        return new PaymentSuccessResponseDto(
                payment.getAmount(),
                payment.getOrderName(),
                payment.getOrderId(),
                payment.getPaymentKey()
        );
    }

    public static PaymentSuccessResponseDto forSuccess(String paymentKey, String orderId, Long amount) {
        return new PaymentSuccessResponseDto(
                amount,
                orderId,
                paymentKey
        );
    }
}
