package nbc_final.gathering.domain.ad.dto.response;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class AdCreateResponseDto {
    private final Long adId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final long totalAmount; // 총 금액
    private final String orderName; // 광고 형태 추가
    private final long adDuration; // 광고 기간

    public AdCreateResponseDto(Long adId, LocalDate startDate, LocalDate endDate, long totalAmount, String orderName, long adDuration) {
        this.adId = adId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalAmount = totalAmount;
        this.orderName = orderName;
        this.adDuration = adDuration;
    }

    public static AdCreateResponseDto of(Long adId, LocalDate startDate, LocalDate endDate, long totalAmount, String orderName, long adDuration) {
        return new AdCreateResponseDto(adId, startDate, endDate, totalAmount, orderName, adDuration);
    }


}
