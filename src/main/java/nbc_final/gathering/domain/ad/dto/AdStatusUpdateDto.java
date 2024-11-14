package nbc_final.gathering.domain.ad.dto;

import lombok.Getter;

@Getter
public class AdStatusUpdateDto {
    private Long adId;
    private boolean isActive;

    public static AdStatusUpdateDto of(Long adId, boolean isActive) {
        AdStatusUpdateDto dto = new AdStatusUpdateDto();
        dto.adId = adId;
        dto.isActive = isActive;
        return dto;
    }
}
