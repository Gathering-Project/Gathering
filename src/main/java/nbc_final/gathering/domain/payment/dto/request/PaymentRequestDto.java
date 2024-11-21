package nbc_final.gathering.domain.payment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class PaymentRequestDto {

    @NotNull(message = "소모임 ID는 필수입니다.")
    private Long gatheringId;

    @NotNull(message = "금액은 필수입니다.")
    private Long amount;

    @NotNull(message = "주문명은 필수입니다.")
    private String orderName;

    @NotNull(message = "시작일은 필수입니다.")
    private LocalDate startDate;

    @NotNull(message = "종료일은 필수입니다.")
    private LocalDate endDate;

    private Long adId;

    private PaymentRequestDto(Long gatheringId, Long amount, String orderName, LocalDate startDate, LocalDate endDate, Long adId) {
        this.gatheringId = gatheringId;
        this.amount = amount;
        this.orderName = orderName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.adId = adId;
    }

    public static PaymentRequestDto of(Long gatheringId, Long amount, String orderName, LocalDate startDate, LocalDate endDate, Long adId) {
        return new PaymentRequestDto(gatheringId, amount, orderName, startDate, endDate, adId);
    }
}
