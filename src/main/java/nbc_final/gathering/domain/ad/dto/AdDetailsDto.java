package nbc_final.gathering.domain.ad.dto;

import lombok.Getter;
import nbc_final.gathering.domain.ad.entity.Ad;
import nbc_final.gathering.domain.ad.entity.AdStatus;

import java.time.LocalDate;

@Getter
public class AdDetailsDto {
    private final Long adId;
    private final String gatheringName;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final AdStatus status;

    private AdDetailsDto(Long adId, String gatheringName, LocalDate startDate, LocalDate endDate, AdStatus status) {
        this.adId = adId;
        this.gatheringName = gatheringName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public static AdDetailsDto from(Ad ad) {
        return new AdDetailsDto(
                ad.getAdId(),
                ad.getGathering().getTitle(),
                ad.getStartDate(),
                ad.getEndDate(),
                ad.getStatus()
        );
    }
}
