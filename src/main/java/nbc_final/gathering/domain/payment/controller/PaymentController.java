package nbc_final.gathering.domain.payment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ApiResponse;
import nbc_final.gathering.domain.payment.dto.request.PaymentRequestDto;
import nbc_final.gathering.domain.payment.dto.response.PaymentCancelRequestDto;
import nbc_final.gathering.domain.payment.dto.response.PaymentSuccessResponseDto;
import nbc_final.gathering.domain.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/v2/payments/ad")
    public ResponseEntity<ApiResponse<?>> requestAdPayment(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody PaymentRequestDto requestDto) {
        log.info("requestAd : -----------------------------------------------");
        PaymentSuccessResponseDto paymentResDto = paymentService.requestPayment(authUser.getUserId(), requestDto);

        log.info("API 응답 데이터: {}", paymentResDto); // 로그 추가
        return ResponseEntity.ok(ApiResponse.createSuccess(paymentResDto));
    }

    @PostMapping("/v2/payments/success")
    public ResponseEntity<ApiResponse<?>> paymentSuccess(
            @RequestBody PaymentSuccessResponseDto paymentData) {
        log.info("paymentSuccess 호출: paymentKey={}, orderId={}, amount={}",
                paymentData.getPaymentKey(),
                paymentData.getOrderId(),
                paymentData.getAmount());

        String idempotencyKey = "payment:" + paymentData.getOrderId();
        // 서비스 계층에 멱등성 처리 요청
        boolean isProcessed = paymentService.processPaymentIfNotProcessed(
                idempotencyKey,
                paymentData
        );

        if (isProcessed) {
            return ResponseEntity.ok(ApiResponse.createSuccess("결제가 성공적으로 처리되었습니다."));
        } else {
            return ResponseEntity.status(409)
                    .body(ApiResponse.createError(409, "중복된 결제 요청입니다."));
        }
    }

    @PostMapping("/v2/payments/fail")
    public ResponseEntity<ApiResponse<?>> paymentFail(
            @RequestParam String code,
            @RequestParam String message,
            @RequestParam String orderId) {
        log.info("paymentFail 호출: code={}, message={}, orderId={}", code, message, orderId);

        paymentService.handlePaymentFailure(orderId, message);

        return ResponseEntity.status(400)
                .body(ApiResponse.createError(400, message));
    }


    @PostMapping("/v2/payments/{paymentKey}/cancel")
    public ResponseEntity<ApiResponse<?>> cancelPayment(
            @PathVariable String paymentKey,
            @RequestBody PaymentCancelRequestDto cancelRequestDto) {
        paymentService.cancelPayment(paymentKey, cancelRequestDto);
        return ResponseEntity.ok(ApiResponse.createSuccess("결제 취소 완료"));
    }
}
