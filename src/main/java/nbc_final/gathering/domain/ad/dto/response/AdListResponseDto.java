package nbc_final.gathering.domain.ad.dto.response;

import lombok.Getter;
import nbc_final.gathering.domain.ad.dto.AdDetailsDto;

import java.util.List;

@Getter
public class AdListResponseDto {
    private final List<AdDetailsDto> ads;
    private final long adCount;

    private AdListResponseDto(List<AdDetailsDto> ads, long adCount) {
        this.ads = ads;
        this.adCount = adCount;
    }

    public static AdListResponseDto of(List<AdDetailsDto> ads, long adCount) {
        return new AdListResponseDto(ads, adCount);
    }
}
