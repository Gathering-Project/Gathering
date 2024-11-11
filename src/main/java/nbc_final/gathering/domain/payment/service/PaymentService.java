package nbc_final.gathering.domain.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc_final.gathering.common.exception.ResponseCode;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.domain.payment.dto.request.PaymentAdRequest;
import nbc_final.gathering.domain.payment.dto.response.PaymentAdResponse;
import nbc_final.gathering.domain.payment.entity.Payment;
import nbc_final.gathering.domain.payment.repository.PaymentRepository;
import nbc_final.gathering.domain.user.entity.User;
import nbc_final.gathering.domain.user.repository.UserRepository;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    private String clientKey = "test_ck_LlDJaYngrojQGORJldBlVezGdRpX";
    private String secretKey = "test_sk_GePWvyJnrKJLzBoXdEm7VgLzN97E";

    private static final String TOSS_API_URL = "https://api.tosspayments.com/v1/payments/confirm";

    @Transactional
    public PaymentAdResponse processAdPayment(Long userId, PaymentAdRequest requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));

        Payment payment = Payment.createPayment(user, requestDto);
        paymentRepository.save(payment);

        return payment.toResponseDto();
    }

    @Transactional
    public void approvePayment(String paymentKey, String orderId, Long amount) {
        try {
            // Authorization 헤더 생성
            String auth = secretKey + ":";
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
            String authHeader = "Basic " + new String(encodedAuth);

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", authHeader);

            // 요청 바디 설정
            Map<String, Object> body = new HashMap<>();
            body.put("paymentKey", paymentKey);
            body.put("orderId", orderId);
            body.put("amount", amount);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            // API 호출
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    TOSS_API_URL,
                    request,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                // 결제 승인 성공 처리
                Payment payment = paymentRepository.findByOrderId(orderId)
                        .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT, "해당 주문 ID의 결제 내역을 찾을 수 없습니다."));

                payment.completePayment(paymentKey, amount.intValue());
            } else {
                // 결제 승인 실패 처리
                throw new ResponseCodeException(ResponseCode.TRANSACTION_FAILED, "결제 승인 실패");
            }
        } catch (ResponseCodeException e) {
            throw e;
        } catch (Exception e) {
            log.error("결제 승인 중 예외 발생", e);
            throw new ResponseCodeException(ResponseCode.TRANSACTION_FAILED, "결제 승인 중 오류 발생: " + e.getMessage());
        }
    }
}
