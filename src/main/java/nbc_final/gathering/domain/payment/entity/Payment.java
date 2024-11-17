package nbc_final.gathering.domain.payment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.common.entity.TimeStamped;
import nbc_final.gathering.domain.ad.entity.Ad;
import nbc_final.gathering.domain.gathering.entity.Gathering;

import java.time.LocalDate;

@Entity
@Getter
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

    /**
     * 결제 생성
     */
    public static Payment create(Long amount, String orderName, Gathering gathering, LocalDate startDate, LocalDate endDate) {
        Payment payment = new Payment();
        payment.amount = amount;
        payment.orderName = orderName;
        payment.gathering = gathering;
        payment.startDate = startDate;
        payment.endDate = endDate;
        payment.status = PayStatus.PENDING; // 기본 상태
        return payment;
    }

    /**
     * 결제 요청 업데이트
     */
    public void updateRequest(Long amount, String orderName, String orderId) {
        this.amount = amount;
        this.orderName = orderName;
        this.orderId = orderId;
        this.status = PayStatus.READY;
    }

    /**
     * 결제 완료 처리
     */
    public void completePayment(String paymentKey) {
        this.paymentKey = paymentKey;
        this.status = PayStatus.PAID;
    }

    /**
     * 결제 실패 처리
     */
    public void failPayment(String reason) {
        this.status = PayStatus.FAILED;
        this.failReason = reason;
    }

    /**
     * 결제 취소 처리
     */
    public void cancelPayment(String reason) {
        this.status = PayStatus.CANCELED;
        this.cancelReason = reason;
    }

    /**
     * 주문 ID 설정
     */
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setPaymentKey(String paymentKey) {
        this.paymentKey = paymentKey;
    }
    /**
     * 광고 설정
     */
    public void setAd(Ad ad) {
        this.ad = ad;
    }
}
