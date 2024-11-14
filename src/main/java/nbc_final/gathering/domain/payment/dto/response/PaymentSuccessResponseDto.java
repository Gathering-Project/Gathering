package nbc_final.gathering.domain.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.domain.payment.entity.Payment;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSuccessResponseDto {
    private Long amount;
    private String orderName;
    private String orderId;
    private String paymentKey;  // paymentKey 필드 추가

    // Payment 객체를 받아서 PaymentSuccessResponseDto로 변환하는 메서드
    public static PaymentSuccessResponseDto from(Payment payment) {
        return new PaymentSuccessResponseDto(
                payment.getAmount(),
                payment.getOrderName(),
                payment.getOrderId(),
                payment.getPaymentKey()  // 결제 키를 추가
        );
    }

    // 기존 생성자에 추가로 paymentKey를 포함한 생성자 추가
    public static PaymentSuccessResponseDto forSuccess(String paymentKey, String orderId, Long amount) {
        return new PaymentSuccessResponseDto(
                amount,
                orderId,
                paymentKey,
                paymentKey // paymentKey 필드에 넣어줌
        );
    }

    // 새로운 생성자 추가 (paymentKey를 받는 생성자)
    public PaymentSuccessResponseDto(Long amount, String orderName, String orderId) {
        this.amount = amount;
        this.orderName = orderName;
        this.orderId = orderId;
        this.paymentKey = null; // paymentKey는 기본적으로 null로 설정
    }
}
