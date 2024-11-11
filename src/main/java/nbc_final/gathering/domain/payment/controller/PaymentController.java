package nbc_final.gathering.domain.payment.controller;

import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ApiResponse;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.domain.payment.dto.request.PaymentAdRequest;
import nbc_final.gathering.domain.payment.dto.response.PaymentAdResponse;
import nbc_final.gathering.domain.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@Validated
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/payment")
    public String paymentPage() {
        return "payment";
    }

    @PostMapping("/v2/payments/ad")
    public ResponseEntity<ApiResponse<?>> requestAdPayment(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody PaymentAdRequest requestDto) {
        try {
            PaymentAdResponse paymentResDto = paymentService.processAdPayment(authUser.getUserId(), requestDto);
            return ResponseEntity.ok(ApiResponse.createSuccess(paymentResDto));
        } catch (ResponseCodeException e) {
            return ResponseEntity.status(e.getHttpStatus())
                    .body(ApiResponse.createError(e.getHttpStatus().value(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.createError(500, "내부 서버 오류 발생"));
        }
    }

    @GetMapping("/payment-success")
    public String paymentSuccess(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Long amount,
            Model model) {
        try {
            paymentService.approvePayment(paymentKey, orderId, amount);

            model.addAttribute("paymentKey", paymentKey);
            model.addAttribute("orderId", orderId);
            model.addAttribute("amount", amount);
            return "success";
        } catch (ResponseCodeException e) {
            model.addAttribute("errorCode", e.getHttpStatus().value());
            model.addAttribute("errorMessage", e.getMessage());
            return "fail";
        } catch (Exception e) {
            model.addAttribute("errorCode", 500);
            model.addAttribute("errorMessage", "내부 서버 오류 발생");
            return "fail";
        }
    }

    @GetMapping("/payment-fail")
    public String paymentFail(
            @RequestParam String code,
            @RequestParam String message,
            @RequestParam String orderId,
            Model model) {
        model.addAttribute("errorCode", code);
        model.addAttribute("errorMessage", message);
        model.addAttribute("orderId", orderId);
        return "fail";
    }
}
