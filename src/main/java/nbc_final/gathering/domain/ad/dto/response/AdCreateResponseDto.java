package nbc_final.gathering.domain.ad.dto.response;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class AdCreateResponseDto {
    private final Long adId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final long totalAmount;
    private final String orderName;
    private final long adDuration;

    private AdCreateResponseDto(Long adId, LocalDate startDate, LocalDate endDate, long totalAmount, String orderName, long adDuration) {
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
