package nbc_final.gathering.domain.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PaymentFailureResponse {
    private final String errorCode;    // 에러 코드
    private final String errorMessage; // 에러 메시지
    private final String orderId;      // 주문 ID
}
