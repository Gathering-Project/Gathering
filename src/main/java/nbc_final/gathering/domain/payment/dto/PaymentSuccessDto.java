package nbc_final.gathering.domain.payment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class PaymentSuccessDto {
    private final String paymentKey;
    private final String orderId;
    private final Long amount;
    private final String method;
    private final String status;

    // private 생성자, 빌더 패턴으로 초기화
    private PaymentSuccessDto(String paymentKey, String orderId, Long amount, String method, String status) {
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.amount = amount;
        this.method = method;
        this.status = status;
    }

    // 정적 팩토리 메서드
    public static PaymentSuccessDto of(String paymentKey, String orderId, Long amount, String method, String status) {
        return new PaymentSuccessDto(paymentKey, orderId, amount, method, status);
    }

    // 빌더 메서드가 필요한 경우
    @Builder
    public static PaymentSuccessDto create(String paymentKey, String orderId, Long amount, String method, String status) {
        return new PaymentSuccessDto(paymentKey, orderId, amount, method, status);
    }
}
