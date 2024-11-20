package nbc_final.gathering.domain.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ApiResponse;
import nbc_final.gathering.domain.payment.dto.request.PaymentCancelRequestDto;
import nbc_final.gathering.domain.payment.dto.request.PaymentRequestDto;
import nbc_final.gathering.domain.payment.dto.response.PaymentHistoryResponseDto;
import nbc_final.gathering.domain.payment.dto.response.PaymentSuccessResponseDto;
import nbc_final.gathering.domain.payment.service.PaymentService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Payment API", description = "광고 결제 관련 API 모음입니다.")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 결제 요청
     *
     * @param authUser   인증된 사용자 정보
     * @param requestDto 결제 요청 데이터
     * @return 결제 요청 결과
     */
    @Operation(summary = "결제 요청", description = "결제를 요청합니다.")
    @PostMapping("/v2/payments/ad")
    public ResponseEntity<ApiResponse<?>> requestAdPayment(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody PaymentRequestDto requestDto) {
        PaymentSuccessResponseDto responseDto = paymentService.requestPayment(authUser.getUserId(), requestDto);
        return ResponseEntity.ok(ApiResponse.createSuccess(responseDto));
    }

    /**
     * 결제 승인 처리
     *
     * @param paymentData 결제 승인 데이터
     * @return 승인 처리 완료 메시지
     */
    @Operation(summary = "결제 승인", description = "결제 요청을 승인합니다.")
    @PostMapping("/v2/payments/success")
    public ResponseEntity<ApiResponse<?>> paymentSuccess(@RequestBody PaymentSuccessResponseDto paymentData) {
        paymentService.approvePayment(paymentData.getPaymentKey(), paymentData.getOrderId(), paymentData.getAmount());
        return ResponseEntity.ok(ApiResponse.createSuccess("결제 성공 처리 완료"));
    }

    /**
     * 결제 실패 처리
     *
     * @param failData 실패 요청 데이터 (orderId, 이유)
     * @return 실패 처리 완료 메시지
     */
    @Operation(summary = "결제 실패 처리", description = "요청된 결제에 대한 실패 처리를 진행합니다.")
    @PostMapping("/v2/payments/fail")
    public ResponseEntity<ApiResponse<?>> paymentFail(
            @RequestBody Map<String, String> failData) {
        String orderId = failData.get("orderId");
        String reason = failData.get("reason");

        paymentService.handlePaymentFailure(orderId, reason);

        return ResponseEntity.ok(ApiResponse.createSuccess("결제 실패 처리 완료"));
    }

    /**
     * 결제 취소 처리
     *
     * @param paymentKey       결제 키
     * @param cancelRequestDto 결제 취소 요청 데이터
     * @return 취소 처리 완료 메시지
     */
    @Operation(summary = "결제 취소(환불)", description = "이미 승인된 결제에 대해 환불 처리합니다.")
    @PostMapping("/v2/payments/{paymentKey}/cancel")
    public ResponseEntity<ApiResponse<?>> cancelPayment(
            @PathVariable String paymentKey,
            @RequestBody PaymentCancelRequestDto cancelRequestDto) {
        paymentService.cancelPayment(paymentKey, cancelRequestDto);
        log.info("결제 취소 처리 완료: Payment Key = {}, Reason = {}", paymentKey, cancelRequestDto.getCancelReason());
        return ResponseEntity.ok(ApiResponse.createSuccess("결제 취소 처리 완료"));
    }

    /**
     * 결제 조회
     *
     * @param authUser 인증된 사용자 정보
     * @param orderId  주문 ID
     * @return 결제 상세 정보
     */
    @Operation(summary = "결제 단건 조회", description = "특정 결제 내역에 대해 조회합니다.")
    @GetMapping("/v2/payments/{orderId}")
    public ResponseEntity<ApiResponse<?>> getPayment(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable String orderId) {
        log.info("결제 조회 요청: userId = {}, Order ID = {}", authUser.getUserId(), orderId);
        PaymentSuccessResponseDto payment = paymentService.getPaymentDetails(orderId, authUser.getUserId());
        return ResponseEntity.ok(ApiResponse.createSuccess(payment));
    }

    /**
     * 모든 결제 내역 조회
     *
     * @param authUser 인증된 사용자 정보
     * @param page     페이지 번호
     * @param size     페이지 크기
     * @return 결제 내역 페이지
     */
    @Operation(summary = "모든 결제 내역 조회", description = "모든 결제 내역에 대해 조회합니다.")
    @GetMapping("/v2/payments/history")
    public ResponseEntity<ApiResponse<?>> getAllPayments(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Page<PaymentHistoryResponseDto> payments = paymentService.getPaymentHistory(authUser.getUserId(), page, size);
        return ResponseEntity.ok(ApiResponse.createSuccess(payments));
    }
}
