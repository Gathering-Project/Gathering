package nbc_final.gathering.domain.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc_final.gathering.common.exception.ResponseCode;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.domain.ad.entity.Ad;
import nbc_final.gathering.domain.ad.entity.AdStatus;
import nbc_final.gathering.domain.ad.repository.AdRepository;
import nbc_final.gathering.domain.member.entity.Member;
import nbc_final.gathering.domain.member.enums.MemberRole;
import nbc_final.gathering.domain.member.repository.MemberRepository;
import nbc_final.gathering.domain.payment.dto.request.PaymentRequestDto;
import nbc_final.gathering.domain.payment.dto.request.PaymentCancelRequestDto;
import nbc_final.gathering.domain.payment.dto.response.PaymentHistoryResponseDto;
import nbc_final.gathering.domain.payment.dto.response.PaymentSuccessResponseDto;
import nbc_final.gathering.domain.payment.entity.PayStatus;
import nbc_final.gathering.domain.payment.entity.Payment;
import nbc_final.gathering.domain.payment.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;
    private final RestTemplate restTemplate;
    private final AdRepository adRepository;

    @Value("${payment.toss.url}")
    private String tossUrl;

    @Value("${payment.toss.secret.key}")
    private String tossSecretKey;

    @Value("${payment.toss.cancel.url}")
    private String tossCancelUrl;

    private final RedisTemplate<String, Object> redisTemplate;

    private static final long IDEMPOTENCY_KEY_TTL_SECONDS = 1800; // 30분
    private static final long PAYMENT_TIMEOUT_SECONDS = 1800; // 30분

    // 결제 요청
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public PaymentSuccessResponseDto requestPayment(Long userId, PaymentRequestDto requestDto) {
        try {
            log.info("결제 요청: userId = {}, adId = {}, amount = {}, orderName = {}",
                    userId, requestDto.getAdId(), requestDto.getAmount(), requestDto.getOrderName());

            // 사용자가 소모임의 HOST인지 검증
            Member member = memberRepository.findByUserIdAndGatheringIdAndRole(userId, requestDto.getGatheringId(), MemberRole.HOST)
                    .orElseThrow(() -> new ResponseCodeException(ResponseCode.FORBIDDEN));

            // 광고 존재 여부 검증
            Ad ad = adRepository.findById(requestDto.getAdId())
                    .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_AD));

            // 결제 데이터 검증
            Payment payment = paymentRepository.findByAd_AdId(ad.getAdId())
                    .orElseThrow(() -> new ResponseCodeException(ResponseCode.PAYMENT_NOT_FOUND));

            // 주문 및 결제 키 생성
            Map<String, String> keys = createOrderAndPaymentKey(payment.getPaymentId());
            String orderId = keys.get("orderId");
            String paymentKey = keys.get("paymentKey");

            // 멱등성 키 저장
            if (!saveIdempotencyKey(payment.getPaymentId().toString(), "request", createIdempotencyKey())) {
                throw new ResponseCodeException(ResponseCode.PAYMENTS_CONFLICT);
            }

            // 결제 요청 정보 업데이트
            payment.updateRequest(requestDto.getAmount(), requestDto.getOrderName(), orderId);
            payment.setPaymentKey(paymentKey);
            paymentRepository.save(payment);

            return PaymentSuccessResponseDto.from(payment);
        } catch (ResponseCodeException ex) {
            throw ex;
        } catch (Exception e) {
            throw new ResponseCodeException(ResponseCode.PAYMENT_REQUEST_FAILED);
        }
    }

    //결제 승인
    @Transactional
    public void approvePayment(String paymentKey, String orderId, Long amount) {
        if (isPaymentAlreadyApproved(orderId)) {
            return;
        }

        if (sendApprovalRequestToToss(paymentKey, orderId, amount)) {
            Payment payment = paymentRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new ResponseCodeException(ResponseCode.PAYMENT_NOT_FOUND));

            // 결제 완료 처리
            payment.completePayment(paymentKey);
            paymentRepository.save(payment);

            // 광고 상태 업데이트
            Ad ad = payment.getAd();
            ad.updateStatus(AdStatus.PAID);
            adRepository.save(ad);

            log.info("결제 승인 완료 및 광고 PAID 상태 업데이트: orderId={}, adId={}", orderId, ad.getAdId());
        } else {
            throw new ResponseCodeException(ResponseCode.TRANSACTION_FAILED);
        }
    }

    //결제 실패
    @Transactional
    public void handlePaymentFailure(String orderId, String reason) {
        log.info("결제 실패 처리 시작: Order ID = {}, Reason = {}", orderId, reason);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.PAYMENT_NOT_FOUND));

        payment.failPayment(reason);
        paymentRepository.save(payment);

        Ad ad = payment.getAd();
        ad.updateStatus(AdStatus.FAILED);
        adRepository.save(ad);

        log.info("결제 실패 및 광고 상태 업데이트 완료: Order ID = {}, Ad ID = {}", orderId, ad.getAdId());
    }

    // 결제 취소
    @Transactional
    public void cancelPayment(String paymentKey, PaymentCancelRequestDto cancelRequestDto) {
        log.info("결제 취소 요청: Payment Key = {}, Cancel Reason = {}", paymentKey, cancelRequestDto.getCancelReason());

        Payment payment = paymentRepository.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.PAYMENT_NOT_FOUND));

        Ad ad = payment.getAd();
        log.info("현재 광고 상태: {}", ad.getStatus());
        if (ad.getStatus() != AdStatus.PENDING && ad.getStatus() != AdStatus.PAID) {
            throw new ResponseCodeException(ResponseCode.AD_INVALID_REQUEST);
        }

        // 멱등키 생성 및 저장
        String idempotencyKey = createIdempotencyKey();
        if (!saveIdempotencyKey(payment.getPaymentId().toString(), "cancel", idempotencyKey)) {
            throw new ResponseCodeException(ResponseCode.PAYMENTS_CONFLICT);
        }

        // 결제 취소 요청
        if (sendCancelRequestToToss(paymentKey, cancelRequestDto, idempotencyKey)) {

            // 결제 취소 처리
            payment.cancelPayment(cancelRequestDto.getCancelReason());
            paymentRepository.save(payment);

            // 광고 상태 CANCELED로 업데이트
            ad.updateStatus(AdStatus.CANCELED);
            adRepository.save(ad);

            log.info("결제 취소 및 광고 상태 업데이트 완료: Payment Key = {}, Ad ID = {}", paymentKey, ad.getAdId());
        } else {
            throw new ResponseCodeException(ResponseCode.PAYMENT_REQUEST_FAILED);
        }
    }

    // 결제 상세 정보 조회
    public PaymentSuccessResponseDto getPaymentDetails(String orderId, Long userId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.PAYMENT_NOT_FOUND));

        if (!payment.getGathering().getUserId().equals(userId)) {
            throw new ResponseCodeException(ResponseCode.FORBIDDEN);
        }

        String redisKey = "payments:timeout:" + orderId;
        redisTemplate.opsForValue().set(redisKey, "PENDING", PAYMENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        return PaymentSuccessResponseDto.from(payment);
    }

    public Page<PaymentHistoryResponseDto> getPaymentHistory(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Payment> payments = paymentRepository.findAllByGathering_UserId(userId, pageable);
        return payments.map(PaymentHistoryResponseDto::from);
    }



    // ------- 유틸 메서드 -------

    // 주문 ID, 결제키 생성
    private Map<String, String> createOrderAndPaymentKey(Long paymentId) {
        String orderId = "AD_" + paymentId + "_" + String.format("%06d", new Random().nextInt(999999));
        String paymentKey = "Payment_" + UUID.randomUUID();
        return Map.of("orderId", orderId, "paymentKey", paymentKey);
    }

    //멱동키 생성
    private String createIdempotencyKey() {
        return "Idempotency_" + UUID.randomUUID();
    }

    // 멱등키 확인 및 저장
    private boolean saveIdempotencyKey(String paymentId, String requestType, String idempotencyKey) {
        String redisKey = "payments:" + paymentId + ":" + requestType;

        Boolean isNew = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, idempotencyKey, IDEMPOTENCY_KEY_TTL_SECONDS, TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(isNew)) {
            log.info("멱등키 저장 성공: Redis Key = {}, Idempotency Key = {}", redisKey, idempotencyKey);
            return true;
        }

        return false;
    }

    // 토스 API 요청 헤더 생성
    private HttpHeaders createHeaders(String idempotencyKey) {
        String auth = tossSecretKey + ":";
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.set("Idempotency-Key", idempotencyKey);
        return headers;
    }

    // 토스 결제 승인 요청
    private boolean sendApprovalRequestToToss(String paymentKey, String orderId, Long amount) {
        HttpHeaders headers = createHeaders(orderId);
        Map<String, Object> body = Map.of(
                "paymentKey", paymentKey,
                "orderId", orderId,
                "amount", amount
        );

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tossUrl, new HttpEntity<>(body, headers), Map.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            return false;
        }
    }

    // 결제 상태 확인
    private boolean isPaymentAlreadyApproved(String orderId) {
        return paymentRepository.existsByOrderIdAndStatus(orderId, PayStatus.PAID);
    }

    // 토스 결제 취소 요청
    private boolean sendCancelRequestToToss(String paymentKey, PaymentCancelRequestDto cancelRequestDto, String idempotencyKey) {
        String cancelUrl = tossCancelUrl + "/" + paymentKey + "/cancel";
        HttpHeaders headers = createHeaders(idempotencyKey);
        Map<String, Object> body = Map.of("cancelReason", cancelRequestDto.getCancelReason());

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    cancelUrl,
                    new HttpEntity<>(body, headers),
                    Map.class
            );

            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.error("Toss Payments 결제 취소 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }

    // 결제 타임아웃
    @Scheduled(fixedRate = 60000)
    public void checkPaymentTimeouts() {
        Set<String> keys = redisTemplate.keys("payments:timeout:*");
        if (keys != null) {
            for (String key : keys) {
                String orderId = key.split(":")[2];
                Payment payment = paymentRepository.findByOrderId(orderId)
                        .orElse(null);
                if (payment != null && payment.getStatus() == PayStatus.PENDING) {
                    log.info("결제 타임아웃 발생: Order ID = {}", orderId);

                    handlePaymentFailure(orderId, "결제 타임아웃 발생");
                    redisTemplate.delete(key);
                }
            }
        }
    }
}
