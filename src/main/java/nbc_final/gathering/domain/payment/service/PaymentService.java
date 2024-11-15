package nbc_final.gathering.domain.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc_final.gathering.common.exception.ResponseCode;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.domain.ad.service.AdService;
import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.member.entity.Member;
import nbc_final.gathering.domain.member.enums.MemberRole;
import nbc_final.gathering.domain.member.repository.MemberRepository;
import nbc_final.gathering.domain.payment.dto.request.PaymentRequestDto;
import nbc_final.gathering.domain.payment.dto.response.PaymentCancelRequestDto;
import nbc_final.gathering.domain.payment.dto.response.PaymentSuccessResponseDto;
import nbc_final.gathering.domain.payment.entity.PayStatus;
import nbc_final.gathering.domain.payment.entity.Payment;
import nbc_final.gathering.domain.payment.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;
    private final RestTemplate restTemplate;
    private final AdService adService;

    @Value("${payment.toss.url}")
    private String tossUrl;

    @Value("${payment.toss.secret.key}")
    private String tossSecretKey;

    // 광고 결제 요청
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public PaymentSuccessResponseDto requestPayment(Long userId, PaymentRequestDto requestDto) {
        Member member = memberRepository.findByUserIdAndGatheringIdAndRole(userId, requestDto.getGatheringId(), MemberRole.HOST)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.FORBIDDEN));

        Gathering gathering = member.getGathering();
        validateRequest(userId, requestDto);

        String orderId = generateOrderId();
        validateOrderIdUniqueness(orderId);

        // 광고 신청 가능 날짜 범위 검증
        adService.validateAdDateRange(requestDto.getGatheringId(), requestDto.getStartDate(), requestDto.getEndDate());

        // 결제 객체 생성 및 저장
        Payment payment = Payment.create(member.getUser(), requestDto.getAmount(), requestDto.getOrderName(), gathering);
        if (!payment.startProcessing()) {
            throw new ResponseCodeException(ResponseCode.CONFLICT, "이미 처리 중인 결제 요청입니다.");
        }

        payment.setOrderId(orderId);
        paymentRepository.save(payment);

        return PaymentSuccessResponseDto.from(payment);
    }

    // 결제 승인 처리
    @Transactional
    public void approvePayment(String paymentKey, String orderId, Long amount) {
        if (isPaymentAlreadyApproved(orderId)) {
            log.info("중복 요청 방지: 이미 승인된 결제입니다: {}", orderId);
            return;
        }

        if (sendApprovalRequestToToss(paymentKey, orderId, amount)) {
            Payment payment = paymentRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT, "주문 ID에 대한 결제 내역이 없습니다."));

            payment.completePayment(paymentKey, amount.intValue());
            paymentRepository.save(payment);
            log.info("결제 승인 완료: orderId={}", orderId);
        } else {
            throw new ResponseCodeException(ResponseCode.TRANSACTION_FAILED, "결제 승인 실패");
        }
    }

    // 결제 실패 처리
    @Transactional
    public void handlePaymentFailure(String orderId, String failReason) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT, "주문 ID에 대한 결제 내역이 없습니다."));
        payment.failPayment(failReason);
        paymentRepository.save(payment);
        log.info("결제 실패가 기록되었습니다: orderId = {}, failReason = {}", orderId, failReason);
    }

    // 결제 취소 처리
    @Transactional
    public void cancelPayment(String paymentKey, PaymentCancelRequestDto cancelRequestDto) {
        Payment payment = paymentRepository.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT, "PaymentKey에 대한 결제 내역이 없습니다."));

        // 광고 게시 중인지 확인
        if (payment.getAd().isActive()) {
            throw new ResponseCodeException(ResponseCode.INVALID_REQUEST, "광고가 이미 게시 중이므로 취소할 수 없습니다.");
        }

        // Toss Payments 결제 취소 API 호출
        if (sendCancelRequestToToss(paymentKey, cancelRequestDto)) {
            payment.cancelPayment(cancelRequestDto.getCancelReason());
            log.info("결제 상태 업데이트: status={}, cancelReason={}", payment.getStatus(), payment.getCancelReason());
            paymentRepository.save(payment);
            log.info("결제가 취소되었습니다: paymentKey = {}, cancelReason = {}", paymentKey, cancelRequestDto.getCancelReason());
        } else {
            throw new ResponseCodeException(ResponseCode.TRANSACTION_FAILED, "결제 취소 실패");
        }
    }

    // Toss Payments 결제 승인 요청
    private boolean sendApprovalRequestToToss(String paymentKey, String orderId, Long amount) {
        HttpHeaders headers = createHeaders(orderId);
        Map<String, Object> body = Map.of(
                "paymentKey", paymentKey,
                "orderId", orderId,
                "amount", amount
        );

        for (int i = 0; i < 3; i++) { // 최대 3회 재시도
            try {
                ResponseEntity<Map> response = restTemplate.postForEntity(tossUrl, new HttpEntity<>(body, headers), Map.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    return true;
                } else {
                    log.error("결제 승인 실패: {}", response.getBody());
                    Thread.sleep(2000); // 2초 대기 후 재시도
                }
            } catch (Exception e) {
                log.error("멱등키 대기: {}", e.getMessage());
            }
        }
        throw new ResponseCodeException(ResponseCode.TRANSACTION_FAILED, "결제 승인 실패");
    }

    // Toss Payments 결제 취소 요청
    private boolean sendCancelRequestToToss(String paymentKey, PaymentCancelRequestDto cancelRequestDto) {
        HttpHeaders headers = createHeaders(paymentKey);
        Map<String, Object> body = Map.of(
                "cancelReason", cancelRequestDto.getCancelReason()
        );

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    tossUrl + "/v1/payments/" + paymentKey + "/cancel",
                    new HttpEntity<>(body, headers),
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("결제 취소 성공: {}", response.getBody());
                return true;
            } else {
                log.error("결제 취소 실패: {}", response.getBody());
                return false;
            }
        } catch (Exception e) {
            log.error("결제 취소 중 오류 발생: {}", e.getMessage());
            throw new ResponseCodeException(ResponseCode.TRANSACTION_FAILED, "결제 취소 중 오류 발생");
        }
    }

    // 요청 검증
    private void validateRequest(Long userId, PaymentRequestDto requestDto) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID는 null이 될 수 없습니다.");
        }
        if (requestDto.getGatheringId() == null) {
            throw new ResponseCodeException(ResponseCode.INVALID_REQUEST, "소모임 ID는 필수 입력 항목입니다.");
        }
    }

    // 주문 ID 생성
    private String generateOrderId() {
        return "Ad_" + UUID.randomUUID();
    }

    // 주문 ID 중복 확인
    private void validateOrderIdUniqueness(String orderId) {
        if (paymentRepository.existsByOrderId(orderId)) {
            throw new ResponseCodeException(ResponseCode.CONFLICT, "이미 존재하는 주문 ID입니다.");
        }
    }

    // 결제가 이미 승인된 상태인지 확인
    private boolean isPaymentAlreadyApproved(String orderId) {
        return paymentRepository.existsByOrderIdAndStatus(orderId, PayStatus.PAID);
    }

    // Toss API 요청 헤더 생성
    private HttpHeaders createHeaders(String idempotencyKey) {
        String auth = tossSecretKey + ":";
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.set("Idempotency-Key", idempotencyKey); // 멱등키 추가
        return headers;
    }
}
