package nbc_final.gathering.domain.payment.dto.response;

import lombok.Getter;
import nbc_final.gathering.domain.payment.entity.PayStatus;
import nbc_final.gathering.domain.payment.entity.PayType;

@Getter
public class PaymentAdResponse {
    private final Long amount;          // 결제 금액
    private final String orderName;     // 주문명
    private final String orderId;       // 주문 ID
    private final PayStatus status;     // 결제 상태
    private final PayType payType;      // 결제 유형

    private PaymentAdResponse(Long amount, String orderName, String orderId, PayStatus status, PayType payType) {
        this.amount = amount;
        this.orderName = orderName;
        this.orderId = orderId;
        this.status = status;
        this.payType = payType;
    }

    // 정적 팩토리 메서드
    public static PaymentAdResponse from(Long amount, String orderName, String orderId, PayStatus status, PayType payType) {
        return new PaymentAdResponse(amount, orderName, orderId, status, payType);
    }
}
