package nbc_final.gathering.domain.ad.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.common.entity.TimeStamped;
import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.payment.entity.Payment;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "ads")
public class Ad extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_id", nullable = false)
    private Gathering gathering;

    @Enumerated(EnumType.STRING)
    private AdStatus status;

    private LocalDate startDate;
    private LocalDate endDate;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "payment_id", unique = true)
    private Payment payment;

    private long totalAmount;

    private String orderName;

    private Ad(Gathering gathering, LocalDate startDate, LocalDate endDate, Long amount) {
        this.gathering = gathering;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = AdStatus.PENDING;
        this.orderName = generateOrderName(startDate, endDate);
        this.totalAmount = amount;
    }

    public static Ad of(Gathering gathering, LocalDate startDate, LocalDate endDate, Long amount) {
        if (gathering == null) {
            throw new IllegalArgumentException("Gathering must not be null.");
        }
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Invalid date range for ad.");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }

        return new Ad(gathering, startDate, endDate, amount);
    }

    public void updateStatus(AdStatus status) {
        this.status = status;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
        payment.setAd(this);
    }

    private static String generateOrderName(LocalDate startDate, LocalDate endDate) {
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        return days + "일 광고";
    }
}
