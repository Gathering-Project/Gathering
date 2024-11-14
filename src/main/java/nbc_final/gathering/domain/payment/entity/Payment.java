package nbc_final.gathering.domain.payment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.common.entity.TimeStamped;
import nbc_final.gathering.domain.ad.entity.Ad;
import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.user.entity.User;

@Getter
@Entity
@Table(name = "payments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    private Long amount;
    private String orderName;

    @Column(unique = true)
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PayStatus status;

    private String paymentKey;
    private String failReason;
    private String cancelReason;

    private boolean isProcessing = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Gathering gathering;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_id", referencedColumnName = "adId")
    private Ad ad;  // Ad와의 연관관계 추가

    // setAd 메서드 추가
    public void setAd(Ad ad) {
        this.ad = ad;
    }

    // startProcessing 메서드 추가
    public boolean startProcessing() {
        if (isProcessing) {
            return false; // 이미 처리 중이면 false 반환
        }
        isProcessing = true;
        return true; // 처리 시작
    }

    // setOrderId 메서드 추가
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public static Payment create(User user, Long amount, String orderName, Gathering gathering) {
        Payment payment = new Payment();
        payment.user = user;
        payment.amount = amount;
        payment.orderName = orderName;
        payment.gathering = gathering;
        payment.status = PayStatus.READY; // READY 상태로 초기화
        return payment;
    }

    public void failPayment(String failReason) {
        this.status = PayStatus.FAILED;
        this.failReason = failReason;
    }

    public void completePayment(String paymentKey, int amount) {
        this.paymentKey = paymentKey;
        this.amount = (long) amount;
        this.status = PayStatus.PAID;
    }

    public void cancelPayment(String cancelReason) {
        this.status = PayStatus.CANCELED;
        this.cancelReason = cancelReason;
    }

    public boolean isPaySuccess() {
        return this.status == PayStatus.PAID;
    }
}
