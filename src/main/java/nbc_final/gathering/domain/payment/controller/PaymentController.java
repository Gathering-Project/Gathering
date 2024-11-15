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
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 광고 결제 요청
     */
    @Transactional
    @PostMapping("/v2/payments/ad")
    public ResponseEntity<ApiResponse<?>> requestAdPayment(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody PaymentRequestDto requestDto) {
        PaymentSuccessResponseDto paymentResDto = paymentService.requestPayment(authUser.getUserId(), requestDto);
        return ResponseEntity.ok(ApiResponse.createSuccess(paymentResDto));
    }

    /**
     * 결제 성공 처리
     */
    @Transactional
    @PostMapping("/v2/payments/success")
    public ResponseEntity<ApiResponse<?>> paymentSuccess(
            @RequestBody PaymentSuccessResponseDto paymentData) {
        // 결제 승인 처리
        paymentService.approvePayment(paymentData.getPaymentKey(), paymentData.getOrderId(), paymentData.getAmount());

        // 반환할 결제 세부 정보 구성
        Map<String, Object> paymentDetails = Map.of(
                "amount", paymentData.getAmount(),
                "orderId", paymentData.getOrderId(),
                "paymentKey", paymentData.getPaymentKey()
        );

        // 결제 정보를 응답 데이터에 포함
        return ResponseEntity.ok(ApiResponse.createSuccess(paymentDetails));
    }

    /**
     * 결제 실패 처리
     */
    @Transactional
    @PostMapping("/v2/payments/fail")
    public ResponseEntity<ApiResponse<?>> paymentFail(
            @RequestParam String code,
            @RequestParam String message,
            @RequestParam String orderId) {
        paymentService.handlePaymentFailure(orderId, message);
        return ResponseEntity.status(400).body(ApiResponse.createError(400, message));
    }

    /**
     * 결제 취소 처리
     */
    @Transactional
    @PostMapping("/v2/payments/{paymentKey}/cancel")
    public ResponseEntity<ApiResponse<?>> cancelPayment(
            @PathVariable String paymentKey,
            @RequestBody PaymentCancelRequestDto cancelRequestDto) {
        paymentService.cancelPayment(paymentKey, cancelRequestDto);
        return ResponseEntity.ok(ApiResponse.createSuccess("결제 취소 완료"));
    }
}
