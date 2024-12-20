package nbc_final.gathering.domain.ad.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class AdCreateRequestDto {

    @NotNull(message = "광고 시작일을 입력해주세요.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "광고 종료일을 입력해주세요.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private AdCreateRequestDto(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static AdCreateRequestDto of(LocalDate startDate, LocalDate endDate) {
        return new AdCreateRequestDto(startDate, endDate);
    }
}
