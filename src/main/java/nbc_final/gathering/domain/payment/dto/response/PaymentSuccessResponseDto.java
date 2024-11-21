package nbc_final.gathering.domain.payment.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc_final.gathering.domain.payment.entity.Payment;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class PaymentSuccessResponseDto {

    private Long amount;
    private String orderName;
    private String orderId;
    private String paymentKey;
    private LocalDate startDate;
    private LocalDate endDate;

    private PaymentSuccessResponseDto(Long amount, String orderName, String orderId, String paymentKey, LocalDate startDate, LocalDate endDate) {
        this.amount = amount;
        this.orderName = orderName;
        this.orderId = orderId;
        this.paymentKey = paymentKey;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static PaymentSuccessResponseDto of(Long amount, String orderName, String orderId, String paymentKey, LocalDate startDate, LocalDate endDate) {
        return new PaymentSuccessResponseDto(amount, orderName, orderId, paymentKey, startDate, endDate);
    }

    public static PaymentSuccessResponseDto from(Payment payment) {
        return new PaymentSuccessResponseDto(
                payment.getAmount(),
                payment.getOrderName(),
                payment.getOrderId(),
                payment.getPaymentKey(),
                payment.getStartDate(),
                payment.getEndDate()
        );
    }
}
