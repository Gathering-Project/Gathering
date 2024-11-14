package nbc_final.gathering.domain.ad.dto;

import lombok.Getter;
import nbc_final.gathering.domain.ad.entity.Ad;
import nbc_final.gathering.domain.gathering.entity.Gathering;

import java.time.LocalDate;

@Getter
public class AdDetailsDto {
    private Long adId;
    private LocalDate startDate;  // LocalDate로 변경
    private LocalDate endDate;    // LocalDate로 변경
    private Gathering gathering;

    public AdDetailsDto(Long adId, LocalDate startDate, LocalDate endDate, Gathering gathering) {
        this.adId = adId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.gathering = gathering;
    }

    public static AdDetailsDto from(Ad ad) {
        return new AdDetailsDto(ad.getAdId(), ad.getStartDate(), ad.getEndDate(), ad.getGathering());
    }
}
