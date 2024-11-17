package nbc_final.gathering.domain.ad.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.payment.entity.Payment;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "ads")
public class Ad {

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

    public static Ad create(Gathering gathering, LocalDate startDate, LocalDate endDate, Long amount)  {
        Ad ad = new Ad();
        ad.gathering = gathering;
        ad.startDate = startDate;
        ad.endDate = endDate;
        ad.status = AdStatus.PENDING;
        ad.orderName = generateOrderName(startDate, endDate);
        ad.totalAmount = amount;
        return ad;
    }

    public void updateStatus(AdStatus status) {
        this.status = status;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    private static String generateOrderName(LocalDate startDate, LocalDate endDate) {
        long days = startDate.until(endDate).getDays() + 1;
        return days + "일 광고";
    }
}
