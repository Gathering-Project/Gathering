package nbc_final.gathering.domain.payment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ApiResponse;
import nbc_final.gathering.domain.payment.dto.request.PaymentRequestDto;
import nbc_final.gathering.domain.payment.dto.response.PaymentCancelRequestDto;
import nbc_final.gathering.domain.payment.dto.response.PaymentHistoryResponseDto;
import nbc_final.gathering.domain.payment.dto.response.PaymentSuccessResponseDto;
import nbc_final.gathering.domain.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/payments")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 결제 요청
     */
    @PostMapping("/ad")
    public ResponseEntity<ApiResponse<?>> requestAdPayment(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody PaymentRequestDto requestDto) {
        log.info("결제 요청 수신: userId={}, requestDto={}", authUser.getUserId(), requestDto);
        PaymentSuccessResponseDto responseDto = paymentService.requestPayment(authUser.getUserId(), requestDto);
        return ResponseEntity.ok(ApiResponse.createSuccess(responseDto));
    }

    /**
     * 결제 승인 처리
     */
    @PostMapping("/success")
    public ResponseEntity<ApiResponse<?>> paymentSuccess(@RequestBody PaymentSuccessResponseDto paymentData) {
        paymentService.approvePayment(paymentData.getPaymentKey(), paymentData.getOrderId(), paymentData.getAmount());
        log.info("결제 승인 처리 완료: Order ID = {}, Payment Key = {}", paymentData.getOrderId(), paymentData.getPaymentKey());
        return ResponseEntity.ok(ApiResponse.createSuccess("결제 성공 처리 완료"));
    }

    /**
     * 결제 실패 처리
     */
    @PostMapping("/fail")
    public ResponseEntity<ApiResponse<?>> paymentFail(
            @RequestParam String orderId,
            @RequestParam String reason) {
        paymentService.handlePaymentFailure(orderId, reason);
        log.info("결제 실패 처리 완료: Order ID = {}, Reason = {}", orderId, reason);
        return ResponseEntity.status(400).body(ApiResponse.createError(400, "결제 실패 처리 완료"));
    }

    /**
     * 결제 취소 처리
     */
    @PostMapping("/{paymentKey}/cancel")
    public ResponseEntity<ApiResponse<?>> cancelPayment(
            @PathVariable String paymentKey,
            @RequestBody PaymentCancelRequestDto cancelRequestDto) {
        paymentService.cancelPayment(paymentKey, cancelRequestDto);
        log.info("결제 취소 처리 완료: Payment Key = {}, Reason = {}", paymentKey, cancelRequestDto.getCancelReason());
        return ResponseEntity.ok(ApiResponse.createSuccess("결제 취소 처리 완료"));
    }

    /**
     * 결제 조회
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<?>> getPayment(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable String orderId) {
        log.info("결제 조회 요청: userId = {}, Order ID = {}", authUser.getUserId(), orderId);
        PaymentSuccessResponseDto payment = paymentService.getPaymentDetails(orderId, authUser.getUserId());
        return ResponseEntity.ok(ApiResponse.createSuccess(payment));
    }

    /**
     * 모든 결제 내역 조회
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<?>> getAllPayments(
            @AuthenticationPrincipal AuthUser authUser) {
        log.info("모든 결제 내역 조회 요청: userId = {}", authUser.getUserId());

        // PaymentHistoryResponseDto로 변경
        List<PaymentHistoryResponseDto> payments = paymentService.getPaymentHistory(authUser.getUserId());

        return ResponseEntity.ok(ApiResponse.createSuccess(payments));
    }
}
