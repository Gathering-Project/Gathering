package nbc_final.gathering.domain.ad.dto.request;

import lombok.Getter;
import java.time.LocalDate;

@Getter
public class AdCreateRequestDto {
    private Long gatheringId;
    private int durationDays;
    private Long userId;
    private LocalDate startDate;  // startDate 필드 추가

    public static AdCreateRequestDto of(Long gatheringId, int durationDays, Long userId, LocalDate startDate) {
        AdCreateRequestDto dto = new AdCreateRequestDto();
        dto.gatheringId = gatheringId;
        dto.durationDays = durationDays;
        dto.userId = userId;
        dto.startDate = startDate;  // startDate 초기화
        return dto;
    }
}
