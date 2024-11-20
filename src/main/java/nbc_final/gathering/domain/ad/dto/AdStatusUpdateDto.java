package nbc_final.gathering.domain.ad.dto;

import lombok.Getter;

@Getter
public class AdStatusUpdateDto {
    private final Long adId;
    private final boolean isActive;

    private AdStatusUpdateDto(Long adId, boolean isActive) {
        this.adId = adId;
        this.isActive = isActive;
    }

    public static AdStatusUpdateDto of(Long adId, boolean isActive) {
        return new AdStatusUpdateDto(adId, isActive);
    }
}
