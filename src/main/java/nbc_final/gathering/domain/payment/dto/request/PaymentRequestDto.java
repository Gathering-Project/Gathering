package nbc_final.gathering.domain.payment.dto.request;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PaymentRequestDto {

    private Long gatheringId;
    private Long adId;
    private Long amount;
    private String orderName;
    private String orderId;
    private LocalDateTime startDate;  // 광고 시작 날짜 필드 추가
    private LocalDateTime endDate;    // 광고 종료 날짜 필드 추가



    public PaymentRequestDto(Long gatheringId, Long adId, Long amount, String orderName, String orderId, LocalDateTime startDate, LocalDateTime endDate) {
        this.gatheringId = gatheringId;
        this.adId = adId;
        this.amount = amount;
        this.orderName = orderName;
        this.orderId = orderId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static PaymentRequestDto of(Long gatheringId, Long adId, Long amount, String orderName, String orderId, LocalDateTime startDate, LocalDateTime endDate) {
        return new PaymentRequestDto(gatheringId, adId, amount, orderName, orderId, startDate, endDate);
    }
}
