package nbc_final.gathering.domain.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc_final.gathering.common.exception.ResponseCode;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.domain.ad.entity.Ad;
import nbc_final.gathering.domain.ad.entity.AdStatus;
import nbc_final.gathering.domain.ad.repository.AdRepository;
import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.member.entity.Member;
import nbc_final.gathering.domain.member.enums.MemberRole;
import nbc_final.gathering.domain.member.repository.MemberRepository;
import nbc_final.gathering.domain.payment.dto.request.PaymentRequestDto;
import nbc_final.gathering.domain.payment.dto.response.PaymentCancelRequestDto;
import nbc_final.gathering.domain.payment.dto.response.PaymentHistoryResponseDto;
import nbc_final.gathering.domain.payment.dto.response.PaymentSuccessResponseDto;
import nbc_final.gathering.domain.payment.entity.PayStatus;
import nbc_final.gathering.domain.payment.entity.Payment;
import nbc_final.gathering.domain.payment.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
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

    /**
     * 결제 요청
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public PaymentSuccessResponseDto requestPayment(Long userId, PaymentRequestDto requestDto) {
        try {
            log.info("결제 요청: userId = {}, adId = {}, amount = {}, orderName = {}",
                    userId, requestDto.getAdId(), requestDto.getAmount(), requestDto.getOrderName());

            Member member = memberRepository.findByUserIdAndGatheringIdAndRole(userId, requestDto.getGatheringId(), MemberRole.HOST)
                    .orElseThrow(() -> new ResponseCodeException(ResponseCode.FORBIDDEN));

            Ad ad = adRepository.findById(requestDto.getAdId())
                    .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_AD, "광고를 찾을 수 없습니다."));

            Payment payment = paymentRepository.findByAd_AdId(ad.getAdId())
                    .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT, "결제 데이터를 찾을 수 없습니다."));

            Map<String, String> keys = generateOrderAndPaymentKey();
            String orderId = keys.get("orderId");
            String paymentKey = keys.get("paymentKey");

            if (!saveIdempotencyKey(payment.getPaymentId().toString(), "request", orderId)) {
                throw new ResponseCodeException(ResponseCode.CONFLICT, "중복된 결제 요청입니다.");
            }

            payment.updateRequest(
                    requestDto.getAmount(),
                    requestDto.getOrderName(),
                    orderId
            );
            payment.setPaymentKey(paymentKey);
            paymentRepository.save(payment);

            log.info("결제 요청 성공: Payment ID = {}, Order ID = {}, Payment Key = {}", payment.getPaymentId(), orderId, paymentKey);

            return PaymentSuccessResponseDto.from(payment);
        } catch (ResponseCodeException ex) {
            log.error("결제 요청 중 오류: {}", ex.getMessage());
            throw ex;
        }
    }

    private Map<String, String> generateOrderAndPaymentKey() {
        String orderId = "Order_" + UUID.randomUUID();
        String paymentKey = "Payment_" + UUID.randomUUID();
        return Map.of("orderId", orderId, "paymentKey", paymentKey);
    }



    /**
     * 결제 승인 처리
     */
    @Transactional
    public void approvePayment(String paymentKey, String orderId, Long amount) {
        if (isPaymentAlreadyApproved(orderId)) {
            log.info("중복 요청 방지: 이미 승인된 결제입니다: {}", orderId);
            return;
        }

        if (sendApprovalRequestToToss(paymentKey, orderId, amount)) {
            Payment payment = paymentRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT, "주문 ID에 대한 결제 내역이 없습니다."));

            // 결제 완료 처리
            payment.completePayment(paymentKey);
            paymentRepository.save(payment);

            // 광고 상태를 PAID로 업데이트
            Ad ad = payment.getAd();
            ad.updateStatus(AdStatus.PAID);
            adRepository.save(ad);

            log.info("결제 승인 완료 및 광고 PAID 상태 업데이트: orderId={}, adId={}", orderId, ad.getAdId());
        } else {
            throw new ResponseCodeException(ResponseCode.TRANSACTION_FAILED, "결제 승인 실패");
        }
    }


    private void updateAdStatusToReady(Long adId) {
        int updatedRows = adRepository.updateAdStatus(adId, AdStatus.PAID);

        if (updatedRows == 0) {
            throw new ResponseCodeException(ResponseCode.NOT_FOUND_AD, "결제와 연결된 광고를 찾을 수 없습니다.");
        }

        log.info("광고 상태가 READY로 업데이트되었습니다: Ad ID = {}", adId);
    }



    /**
     * 결제 실패 처리
     */
    @Transactional
    public void handlePaymentFailure(String orderId, String failReason) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT, "주문 ID에 대한 결제 내역이 없습니다."));
        payment.failPayment(failReason);
        paymentRepository.save(payment);

        // 광고 상태 업데이트
        Ad ad = payment.getAd();
        ad.updateStatus(AdStatus.FAILED);
        adRepository.save(ad);

        log.info("결제 실패 처리 완료: orderId = {}, failReason = {}", orderId, failReason);
    }

    /**
     * 결제 취소 처리
     */
    @Transactional
    public void cancelPayment(String paymentKey, PaymentCancelRequestDto cancelRequestDto) {
        Payment payment = paymentRepository.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT, "PaymentKey에 대한 결제 내역이 없습니다."));

        String idempotencyKey = generateIdempotencyKey();
        if (!saveIdempotencyKey(payment.getPaymentId().toString(), "cancel", idempotencyKey)) {
            throw new ResponseCodeException(ResponseCode.CONFLICT, "중복된 결제 취소 요청입니다.");
        }

        if (sendCancelRequestToToss(paymentKey, cancelRequestDto, idempotencyKey)) {
            payment.cancelPayment(cancelRequestDto.getCancelReason());
            paymentRepository.save(payment);

            Ad ad = payment.getAd();
            ad.updateStatus(AdStatus.CANCELED);
            adRepository.save(ad);

            log.info("결제 취소 완료: Payment Key = {}, Cancel Reason = {}", paymentKey, cancelRequestDto.getCancelReason());
        } else {
            throw new ResponseCodeException(ResponseCode.TRANSACTION_FAILED, "결제 취소 실패");
        }
    }


    // Toss Payments 결제 승인 요청
    private boolean sendApprovalRequestToToss(String paymentKey, String orderId, Long amount) {
        HttpHeaders headers = createHeaders(orderId); // 헤더에 주문 ID 추가
        Map<String, Object> body = Map.of(
                "paymentKey", paymentKey,
                "orderId", orderId,
                "amount", amount
        );

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tossUrl, new HttpEntity<>(body, headers), Map.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.error("결제 승인 요청 실패: {}", e.getMessage());
            return false;
        }
    }

    // Toss Payments 결제 취소 요청
    private boolean sendCancelRequestToToss(String paymentKey, PaymentCancelRequestDto cancelRequestDto, String idempotencyKey) {
        HttpHeaders headers = createHeaders(idempotencyKey);
        Map<String, Object> body = Map.of("cancelReason", cancelRequestDto.getCancelReason());

        try {
            String cancelUrl = tossCancelUrl + "/" + paymentKey + "/cancel"; // 올바른 URL 조합
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    cancelUrl,
                    new HttpEntity<>(body, headers),
                    Map.class
            );
            log.info("Toss Payments 결제 취소 성공: {}", response.getBody());
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.error("Toss Payments 결제 취소 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }

    @Transactional(readOnly = true)
    public PaymentSuccessResponseDto getPaymentDetails(String orderId, Long userId) {
        // 주문 ID로 결제 내역 조회
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT, "주문 ID에 대한 결제 내역이 없습니다."));

        // 권한 검증: 결제가 사용자와 연관되어 있는지 확인
        if (!payment.getGathering().getUserId().equals(userId)) {
            log.warn("권한 없는 결제 조회 시도: userId = {}, orderId = {}", userId, orderId);
            throw new ResponseCodeException(ResponseCode.FORBIDDEN, "결제에 대한 권한이 없습니다.");
        }

        log.info("결제 내역 조회 성공: Order ID = {}, Payment ID = {}", orderId, payment.getPaymentId());
        return PaymentSuccessResponseDto.from(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentHistoryResponseDto> getPaymentHistory(Long userId) {
        List<Payment> payments = paymentRepository.findAllByGathering_UserId(userId);

        if (payments.isEmpty()) {
            log.info("결제 내역이 존재하지 않습니다: userId = {}", userId);
            return Collections.emptyList();
        }

        log.info("결제 내역 조회 성공: userId = {}, 결제 건수 = {}", userId, payments.size());

        return payments.stream()
                .map(PaymentHistoryResponseDto::from) // PaymentHistoryResponseDto로 매핑
                .collect(Collectors.toList());
    }


    // 멱등키 확인 및 저장
    private boolean checkAndSaveIdempotencyKey(String idempotencyKey) {
        String redisKey = "payments:idempotency:" + idempotencyKey;

        Boolean exists = redisTemplate.hasKey(redisKey);
        if (Boolean.TRUE.equals(exists)) {
            log.warn("중복된 멱등키 요청: {}", redisKey);
            return false;
        }

        Boolean isNew = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, "processed", IDEMPOTENCY_KEY_TTL_SECONDS, TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(isNew)) {
            log.info("멱등키 저장 성공: {}", redisKey);
            return true;
        }

        log.error("멱등키 저장 실패: {}", redisKey);
        return false;
    }

    private boolean saveIdempotencyKey(String paymentId, String requestType, String idempotencyKey) {
        String redisKey = "payments:" + paymentId + ":" + requestType;

        Boolean isNew = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, idempotencyKey, IDEMPOTENCY_KEY_TTL_SECONDS, TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(isNew)) {
            log.info("멱등키 저장 성공: Redis Key = {}, Idempotency Key = {}", redisKey, idempotencyKey);
            return true;
        }

        log.warn("중복된 멱등키 요청: Redis Key = {}", redisKey);
        return false;
    }

    // Toss API 요청 헤더 생성
    private HttpHeaders createHeaders(String idempotencyKey) {
        String auth = tossSecretKey + ":";
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.set("Idempotency-Key", idempotencyKey);
        return headers;
    }

    // 주문 ID 생성
    private String generateOrderId() {
        return "Order_" + UUID.randomUUID();
    }

    // 결제가 이미 승인된 상태인지 확인
    private boolean isPaymentAlreadyApproved(String orderId) {
        return paymentRepository.existsByOrderIdAndStatus(orderId, PayStatus.PAID);
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

    private String generateIdempotencyKey() {
        return "Idempotency_" + UUID.randomUUID();
    }
}
