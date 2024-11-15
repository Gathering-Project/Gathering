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
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;
    private final RestTemplate restTemplate;
    private final AdService adService;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate redisTemplate;

    @Value("${payment.toss.url}")
    private String tossUrl;

    @Value("${payment.toss.secret.key}")
    private String tossSecretKey;

//    @Value("${payment.toss.client.key}")
//    private String tossClientKey;

    public PaymentSuccessResponseDto requestPayment(Long userId, PaymentRequestDto requestDto) {
        Member member = memberRepository.findByUserIdAndGatheringIdAndRole(userId, requestDto.getGatheringId(), MemberRole.HOST)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.FORBIDDEN));

        Gathering gathering = member.getGathering();

        String orderId = "Ad_" + UUID.randomUUID();
        if (paymentRepository.existsByOrderId(orderId)) {
            throw new ResponseCodeException(ResponseCode.CONFLICT, "이미 존재하는 주문 ID입니다.");
        }

        adService.validateAdDateRange(requestDto.getGatheringId(), requestDto.getStartDate(), requestDto.getEndDate());

        Payment payment = Payment.create(member.getUser(), requestDto.getAmount(), requestDto.getOrderName(), gathering);

        if (!payment.startProcessing()) {
            throw new ResponseCodeException(ResponseCode.CONFLICT, "이미 처리 중인 결제 요청입니다.");
        }

        payment.setOrderId(orderId);
        paymentRepository.save(payment);

        log.info("생성된 orderId: {}", orderId); // 디버깅을 위해 추가
        return PaymentSuccessResponseDto.from(payment);
    }


    public void approvePaymentWithLock(String paymentKey, String orderId, Long amount) {
        String lockKey = "payment_lock:" + orderId;
        String idempotencyKey = "idempotency:" + orderId + ":" + paymentKey;

        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (lock.tryLock(10, TimeUnit.SECONDS)) {
                // 멱등성 검증
                if (redisTemplate.hasKey(idempotencyKey)) {
                    log.info("이미 처리된 요청입니다. Idempotency Key: {}", idempotencyKey);
                    return;
                }

                // 결제 승인 처리
                approvePayment(paymentKey, orderId, amount);

                // 멱등성 키 저장 (요청이 성공적으로 처리되었음을 기록)
                redisTemplate.opsForValue().set(idempotencyKey, "processed", 1, TimeUnit.HOURS);
            } else {
                throw new ResponseCodeException(ResponseCode.CONFLICT, "동시에 처리 중인 요청입니다.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseCodeException(ResponseCode.TRANSACTION_FAILED, "결제 승인 중 인터럽트 오류 발생");
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public void approvePayment(String paymentKey, String orderId, Long amount) {
        if (isPaymentAlreadyApproved(orderId)) {
            log.info("이미 승인된 결제입니다: {}", orderId);
            return;
        }

        if (sendApprovalRequestToToss(paymentKey, orderId, amount)) {
            Payment payment = paymentRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT, "주문 ID에 대한 결제 내역이 없습니다."));
            payment.completePayment(paymentKey, amount.intValue());
            paymentRepository.save(payment);
        } else {
            throw new ResponseCodeException(ResponseCode.TRANSACTION_FAILED, "결제 승인 실패");
        }
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
            // tossUrl은 이미 전체 URL이므로 추가 경로 불필요
//            log.info("Using Toss Client Key (ck): {}", tossClientKey);
            log.info("Toss API 요청 데이터: {}", body);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    tossUrl, // 그대로 사용
                    new HttpEntity<>(body, headers),
                    Map.class
            );

            log.info("Toss API 응답 데이터: {}", response.getBody());
            return response.getStatusCode() == HttpStatus.OK;
        } catch (HttpClientErrorException e) {
            log.error("Toss API 요청 오류: 상태 코드={}, 응답 바디={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ResponseCodeException(ResponseCode.TRANSACTION_FAILED, "결제 승인 중 오류 발생: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("결제 승인 중 오류 발생: {}", e.getMessage());
            throw new ResponseCodeException(ResponseCode.TRANSACTION_FAILED, "결제 승인 중 오류 발생");
        }
    }

    public void handlePaymentFailure(String orderId, String failReason) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT, "주문 ID에 대한 결제 내역이 없습니다."));
        payment.failPayment(failReason);
        paymentRepository.save(payment);
        log.info("결제 실패 처리 완료: orderId={}, failReason={}", orderId, failReason);
    }

    public void cancelPayment(String paymentKey, PaymentCancelRequestDto cancelRequestDto) {
        Payment payment = paymentRepository.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT, "PaymentKey에 대한 결제 내역이 없습니다."));

        if (payment.getAd().isActive()) {
            throw new ResponseCodeException(ResponseCode.INVALID_REQUEST, "광고가 이미 게시 중이므로 취소할 수 없습니다.");
        }

        if (sendCancelRequestToToss(paymentKey, cancelRequestDto)) {
            payment.cancelPayment(cancelRequestDto.getCancelReason());
            paymentRepository.save(payment);
            log.info("결제가 취소되었습니다: paymentKey = {}, cancelReason = {}", paymentKey, cancelRequestDto.getCancelReason());
        } else {
            throw new ResponseCodeException(ResponseCode.TRANSACTION_FAILED, "결제 취소 실패");
        }
    }

    private HttpHeaders createHeaders(String tossSecretKey) {
        String auth = tossSecretKey + ":"; // 시크릿 키 뒤에 콜론 추가
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8)); // Base64 인코딩
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON); // JSON 포맷 설정
        headers.set("Authorization", "Basic " + encodedAuth); // Basic 인증 형식
        log.info("secretkey : tossSecretKey = {}, encodeAuth : encodeAuth = {}", tossSecretKey,encodedAuth);
        log.info("Authorization Header: {}", "Basic " + encodedAuth);
        return headers;
    }

    @Transactional
    public boolean processPaymentIfNotProcessed(String idempotencyKey, PaymentSuccessResponseDto paymentData) {
        RLock lock = redissonClient.getLock("lock:" + idempotencyKey);

        try {
            if (lock.tryLock(10, TimeUnit.SECONDS)) {
                Boolean isNew = redisTemplate.opsForValue().setIfAbsent(idempotencyKey, "processed", Duration.ofMinutes(10));

                if (Boolean.FALSE.equals(isNew)) {
                    log.info("멱등 키: {}", idempotencyKey);
                    return false; // 이미 처리된 요청
                }

                // 결제 승인 처리
                approvePayment(paymentData.getPaymentKey(), paymentData.getOrderId(), paymentData.getAmount());
                return true;
            } else {
                throw new ResponseCodeException(ResponseCode.CONFLICT, "동시에 처리 중인 요청입니다.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseCodeException(ResponseCode.TRANSACTION_FAILED, "결제 승인 중 인터럽트 오류 발생");
        } finally {
            lock.unlock();
        }
    }

    private boolean sendCancelRequestToToss(String paymentKey, PaymentCancelRequestDto cancelRequestDto) {
        HttpHeaders headers = createHeaders(tossSecretKey);
        Map<String, Object> body = Map.of(
                "cancelReason", cancelRequestDto.getCancelReason()
        );

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    tossUrl + "/v2/payments/" + paymentKey + "/cancel",
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
}
