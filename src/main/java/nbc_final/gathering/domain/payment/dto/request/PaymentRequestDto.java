package nbc_final.gathering.domain.payment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class PaymentRequestDto {

    @NotNull(message = "소모임 ID는 필수 입력 항목입니다.")
    private Long gatheringId;

    @NotNull(message = "결제 금액은 필수 입력 항목입니다.")
    private Long amount;

    @NotNull(message = "주문 이름은 필수 입력 항목입니다.")
    private String orderName;

    @NotNull(message = "시작일은 필수 입력 항목입니다.")
    private LocalDate startDate;  // 광고 시작 날짜 필드

    @NotNull(message = "종료일은 필수 입력 항목입니다.")
    private LocalDate endDate;    // 광고 종료 날짜 필드

    public PaymentRequestDto(Long gatheringId, Long amount, String orderName, LocalDate startDate, LocalDate endDate) {
        this.gatheringId = gatheringId;
        this.amount = amount;
        this.orderName = orderName;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static PaymentRequestDto of(Long gatheringId, Long amount, String orderName, LocalDate startDate, LocalDate endDate) {
        return new PaymentRequestDto(gatheringId, amount, orderName, startDate, endDate);
    }
}
