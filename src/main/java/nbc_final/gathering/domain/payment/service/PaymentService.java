package nbc_final.gathering.domain.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc_final.gathering.common.exception.ResponseCode;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.domain.ad.entity.Ad;
import nbc_final.gathering.domain.ad.repository.AdRepository;
import nbc_final.gathering.domain.ad.service.AdService;
import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.member.entity.Member;
import nbc_final.gathering.domain.member.enums.MemberRole;
import nbc_final.gathering.domain.member.repository.MemberRepository;
import nbc_final.gathering.domain.payment.dto.request.PaymentRequestDto;
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
    private final AdRepository adRepository;
    private final AdService adService;

    @Value("${payment.toss.url}")
    private String tossUrl;

    @Value("${payment.toss.secret.key}")
    private String tossSecretKey;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public PaymentSuccessResponseDto requestPayment(Long userId, PaymentRequestDto requestDto) {
        Member member = memberRepository.findByUserIdAndGatheringIdAndRole(userId, requestDto.getGatheringId(), MemberRole.HOST)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.FORBIDDEN));

        Gathering gathering = member.getGathering();
        String orderId = "Ad_" + UUID.randomUUID().toString();

        if (paymentRepository.existsByOrderId(requestDto.getOrderId())) {
            throw new ResponseCodeException(ResponseCode.CONFLICT, "이미 존재하는 주문 ID입니다.");
        }

        // 광고 신청 가능 날짜 범위 검증
        adService.validateAdDateRange(requestDto.getGatheringId(), requestDto.getStartDate(), requestDto.getEndDate());

        Ad ad = adRepository.findById(requestDto.getAdId())
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT, "광고 ID에 대한 광고 내역이 없습니다."));

        Payment payment = Payment.create(
                member.getUser(), requestDto.getAmount(), requestDto.getOrderName(), ad, gathering
        );

        if (!payment.startProcessing()) {
            throw new ResponseCodeException(ResponseCode.CONFLICT, "이미 처리 중인 결제 요청입니다.");
        }

        payment.setOrderId(orderId);
        paymentRepository.save(payment);

        return PaymentSuccessResponseDto.from(payment);
    }

    @Transactional
    public void approvePayment(String paymentKey, String orderId, Long amount) {
        if (isPaymentAlreadyApproved(orderId)) {
            log.info("이미 승인된 결제입니다: " + orderId);
            return;
        }

        if (sendApprovalRequestToToss(paymentKey, orderId, amount)) {
            Payment payment = paymentRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT, "주문 ID에 대한 결제 내역이 없습니다."));

            payment.completePayment(paymentKey, amount.intValue());
            payment.getAd().activateAd();  // 결제 승인 시 광고 활성화
            paymentRepository.save(payment);
        } else {
            throw new ResponseCodeException(ResponseCode.TRANSACTION_FAILED, "결제 승인 실패");
        }
    }

    @Transactional
    public void handlePaymentFailure(String orderId, String failReason) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT, "주문 ID에 대한 결제 내역이 없습니다."));
        payment.failPayment(failReason);  // 결제 상태 및 실패 이유 업데이트
        paymentRepository.save(payment);
        log.info("결제 실패가 기록되었습니다: orderId = {}, failReason = {}", orderId, failReason);
    }

    @Transactional
    public void cancelPayment(String orderId, String cancelReason) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT, "주문 ID에 대한 결제 내역이 없습니다."));
        payment.cancelPayment(cancelReason);  // 결제 상태 및 취소 이유 업데이트
        paymentRepository.save(payment);
        log.info("결제가 취소되었습니다: orderId = {}, cancelReason = {}", orderId, cancelReason);
    }

    private void saveApprovedPayment(String paymentKey, String orderId, Long amount) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT, "주문 ID에 대한 결제 내역이 없습니다."));
        payment.completePayment(paymentKey, amount.intValue());
        paymentRepository.save(payment);
        log.info("결제가 성공적으로 저장되었습니다: paymentId = {}", payment.getPaymentId());
    }


    private boolean isPaymentAlreadyApproved(String orderId) {
        return paymentRepository.existsByOrderIdAndStatus(orderId, PayStatus.PAID);
    }

    private boolean sendApprovalRequestToToss(String paymentKey, String orderId, Long amount) {
        HttpHeaders headers = createHeaders(tossSecretKey);
        Map<String, Object> body = Map.of(
                "paymentKey", paymentKey,
                "orderId", orderId,
                "amount", amount
        );

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tossUrl, new HttpEntity<>(body, headers), Map.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return true;
            } else {
                log.error("결제 승인 실패: {}", response.getBody());
                return false;
            }
        } catch (Exception e) {
            log.error("결제 승인 중 오류 발생: {}", e.getMessage());
            throw new ResponseCodeException(ResponseCode.TRANSACTION_FAILED, "결제 승인 중 오류 발생");
        }
    }


    private HttpHeaders createHeaders(String tossSecretKey) {
        String auth = tossSecretKey + ":";
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + encodedAuth);
        return headers;
    }
}
