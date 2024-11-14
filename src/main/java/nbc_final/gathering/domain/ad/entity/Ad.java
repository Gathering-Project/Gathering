package nbc_final.gathering.domain.ad.entity;

import jakarta.persistence.*;
import lombok.*;
import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.payment.entity.Payment;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Ad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_id", nullable = false)
    private Gathering gathering;

    @Enumerated(EnumType.STRING)
    private AdStatus status = AdStatus.PENDING;

    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;

    // 광고 객체 생성 메서드
    public static Ad create(Payment payment, Gathering gathering, LocalDate startDate, LocalDate endDate) {
        Ad ad = new Ad();
        ad.setPayment(payment);
        ad.setGathering(gathering);
        ad.setStartDate(startDate);
        ad.setEndDate(endDate);
        ad.setActive(false); // 초기 상태는 비활성화
        return ad;
    }

    public void activateAd() {
        this.status = AdStatus.ACTIVE;
    }

    public void expireAd() {
        this.status = AdStatus.EXPIRED;
    }
}
