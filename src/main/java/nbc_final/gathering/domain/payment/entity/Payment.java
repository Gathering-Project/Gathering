package nbc_final.gathering.domain.payment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.common.entity.TimeStamped;
import nbc_final.gathering.domain.ad.entity.Ad;
import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.user.entity.User;

import java.time.LocalDateTime;

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
    private Ad ad;  // Ad 엔티티와의 연관관계 설정

    // Ad 객체를 파라미터로 받도록 수정
    public static Payment create(User user, Long amount, String orderName, Ad ad, Gathering gathering) {
        Payment payment = new Payment();
        payment.user = user;
        payment.amount = amount;
        payment.orderName = orderName;
        payment.ad = ad;
        payment.gathering = gathering;
        payment.status = PayStatus.READY;
        return payment;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
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

    // 추가된 getter 메서드들
    public Long getPaymentId() {
        return this.paymentId;
    }

    public Ad getAd() {
        return this.ad;
    }

    public synchronized boolean startProcessing() {
        if (isProcessing) {
            return false;
        }
        isProcessing = true;
        return true;
    }

    public synchronized void endProcessing() {
        isProcessing = false;
    }

    public boolean isPaySuccess() {
        return this.status == PayStatus.PAID;
    }
}
