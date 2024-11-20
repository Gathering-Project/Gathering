package nbc_final.gathering.domain.payment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.common.entity.TimeStamped;
import nbc_final.gathering.common.exception.ResponseCode;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.domain.ad.entity.Ad;
import nbc_final.gathering.domain.gathering.entity.Gathering;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
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
    private PayStatus status;

    private String paymentKey;

    @ManyToOne(fetch = FetchType.LAZY)
    private Gathering gathering;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_id")
    private Ad ad;

    private String failReason;
    private String cancelReason;
    private LocalDate startDate;
    private LocalDate endDate;

    public static Payment of(Long amount, String orderName, Gathering gathering, LocalDate startDate, LocalDate endDate) {
        Payment payment = new Payment();
        payment.amount = amount;
        payment.orderName = orderName;
        payment.gathering = gathering;
        payment.startDate = startDate;
        payment.endDate = endDate;
        payment.status = PayStatus.PENDING; // 기본 상태
        return payment;
    }

    public void updateRequest(Long amount, String orderName, String orderId) {
        this.amount = amount;
        this.orderName = orderName;
        this.orderId = orderId;
        this.status = PayStatus.READY;
        updateManualTimestamp();
    }

    public void completePayment(String paymentKey) {
        this.paymentKey = paymentKey;
        this.status = PayStatus.PAID;
        updateManualTimestamp();
    }

    public void failPayment(String failReason) {
        this.status = PayStatus.FAILED;
        this.failReason = failReason;
        updateManualTimestamp();
    }

    public void cancelPayment(String reason) {
        this.status = PayStatus.CANCELED;
        this.cancelReason = reason;
        updateManualTimestamp();
    }

    public void setPaymentKey(String paymentKey) {
        this.paymentKey = paymentKey;
        updateManualTimestamp();
    }

    public void setAd(Ad ad) {
        this.ad = ad;
        updateManualTimestamp();
    }

    private void updateManualTimestamp() {
        try {
            var field = TimeStamped.class.getDeclaredField("updatedAt");
            field.setAccessible(true);
            field.set(this, LocalDateTime.now());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ResponseCodeException(ResponseCode.INTERNAL_SERVER_EXCEPTION);
        }
    }
}
