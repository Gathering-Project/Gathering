package nbc_final.gathering.domain.payment.controller;

import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ApiResponse;
import nbc_final.gathering.domain.payment.dto.request.PaymentRequestDto;
import nbc_final.gathering.domain.payment.dto.response.PaymentSuccessResponseDto;
import nbc_final.gathering.domain.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v2/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/ad")
    public ResponseEntity<ApiResponse<?>> requestAdPayment(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody PaymentRequestDto requestDto) {
        nbc_final.gathering.domain.payment.dto.response.PaymentSuccessResponseDto paymentResDto = paymentService.requestPayment(authUser.getUserId(), requestDto);
        return ResponseEntity.ok(ApiResponse.createSuccess(paymentResDto));
    }

    @PostMapping("/success")
    public ResponseEntity<ApiResponse<?>> paymentSuccess(
            @RequestBody PaymentSuccessResponseDto paymentData) {
        paymentService.approvePayment(
                paymentData.getPaymentKey(),
                paymentData.getOrderId(),
                paymentData.getAmount()
        );
        return ResponseEntity.ok(ApiResponse.createSuccess("결제 승인 완료"));
    }

    @PostMapping("/fail")
    public ResponseEntity<ApiResponse<?>> paymentFail(
            @RequestParam String code,
            @RequestParam String message,
            @RequestParam String orderId) {
        paymentService.handlePaymentFailure(orderId, message);
        return ResponseEntity.status(400)
                .body(ApiResponse.createError(400, message));
    }
}
