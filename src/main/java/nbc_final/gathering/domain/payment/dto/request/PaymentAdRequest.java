package nbc_final.gathering.domain.payment.dto.request;

import lombok.Getter;
import nbc_final.gathering.domain.payment.entity.PayType;

@Getter
public class PaymentAdRequest {
    private final Long adId;           // 광고 ID
    private final Long amount;         // 결제 금액
    private final String orderName;    // 주문명
    private final PayType payType;     // 결제 유형
    private final int durationDays;    // 광고 노출 기간 (일 단위)

    private PaymentAdRequest(Long adId, Long amount, String orderName, PayType payType, int durationDays) {
        this.adId = adId;
        this.amount = amount;
        this.orderName = orderName;
        this.payType = payType;
        this.durationDays = durationDays;
    }

    public static PaymentAdRequest of(Long adId, Long amount, String orderName, PayType payType, int durationDays) {
        return new PaymentAdRequest(adId, amount, orderName, payType, durationDays);
    }

    public PaymentAdRequest withAmount(long amount) {
        return new PaymentAdRequest(this.adId, amount, this.orderName, this.payType, this.durationDays);
    }
}
