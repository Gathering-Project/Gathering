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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_id")
    private Payment Id;

    private String orderName;

    public static Ad create(Gathering gathering, LocalDate startDate, LocalDate endDate) {
        Ad ad = new Ad();
        ad.gathering = gathering;
        ad.startDate = startDate;
        ad.endDate = endDate;
        ad.status = AdStatus.PENDING;
        ad.orderName = generateOrderName(startDate, endDate);  // 광고 형태 생성
        return ad;
    }

    public void updateStatus(AdStatus status) {
        this.status = status;
    }
    // 광고 형태 생성 (startDate ~ endDate)
    private static String generateOrderName(LocalDate startDate, LocalDate endDate) {
        long days = startDate.until(endDate).getDays() + 1;  // 날짜 계산
        return days + "일 광고";  // 예: "3일 광고"
    }
}
