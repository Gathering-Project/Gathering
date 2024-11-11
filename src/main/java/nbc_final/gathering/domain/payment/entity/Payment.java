package nbc_final.gathering.domain.payment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.common.entity.TimeStamped;
import nbc_final.gathering.domain.payment.dto.request.PaymentAdRequest;
import nbc_final.gathering.domain.payment.dto.response.PaymentAdResponse;
import nbc_final.gathering.domain.user.entity.User;

import java.util.UUID;

@Getter
@Entity
@Table(name = "payments")  // 테이블 이름을 명시적으로 지정
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // 기본 생성자에 대한 접근 제어
public class Payment extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    private Long amount;
    private String orderName;
    private String orderId;

    @Enumerated(EnumType.STRING)
    private PayStatus status;

    @Enumerated(EnumType.STRING)
    private PayType payType;

    private String paymentKey;
    private String failReason;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // 광고 ID 필드 추가
    private Long adId;

    // 주 생성자 (기본 생성자와 구별되며 직접 사용)
    private Payment(User user, Long amount, String orderName, PayType payType, Long adId) {
        this.amount = amount;
        this.orderName = orderName;
        this.orderId = UUID.randomUUID().toString();
        this.status = PayStatus.PENDING;
        this.payType = payType;
        this.user = user;
        this.adId = adId;
    }

    // 정적 팩토리 메서드
    public static Payment createPayment(User user, PaymentAdRequest dto) {
        return new Payment(user, dto.getAmount(), dto.getOrderName(), dto.getPayType(), dto.getAdId());
    }

    public void setPaySuccess(String paymentKey) {
        this.paymentKey = paymentKey;
        this.status = PayStatus.SUCCESS;
    }

    public void failPayment(String code, String message) {
        this.status = PayStatus.FAILED;
        this.failReason = message;
    }

    // 결제 성공 여부 확인 메서드
    public boolean isPaySuccess() {
        return this.status == PayStatus.SUCCESS;
    }

    // 결제 완료 메서드
    public void completePayment(String paymentKey, int amount) {
        this.paymentKey = paymentKey;
        this.amount = (long) amount;
        this.status = PayStatus.SUCCESS;
    }

    // 광고 ID 반환 메서드
    public Long getAdId() {
        return adId;
    }

    public PaymentAdResponse toResponseDto() {
        return PaymentAdResponse.from(amount, orderName, orderId, status, payType);
    }

    public void cancelPayment(String cancelReason) {
        this.status = PayStatus.CANCELED;
        this.failReason = cancelReason;
    }
}
