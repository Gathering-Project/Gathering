package nbc_final.gathering.common.config;

import nbc_final.gathering.domain.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.assertTrue; // 추가
import static org.junit.jupiter.api.Assertions.assertFalse; // 추가
import static org.junit.jupiter.api.Assertions.assertEquals; // 추가

@SpringBootTest
public class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    public void testIdempotencyKeyWithPaymentId() {
        Long paymentId = 12345L; // 가상 Payment ID
        String idempotencyKey = "idempotency:" + paymentId;

        // 첫 저장
        boolean firstSave = paymentService.checkAndSaveIdempotencyKey(idempotencyKey, paymentId);
        assertTrue(firstSave);

        // 중복 저장
        boolean duplicateSave = paymentService.checkAndSaveIdempotencyKey(idempotencyKey, paymentId);
        assertFalse(duplicateSave);

        // Redis에서 값 확인
        String storedValue = (String) redisTemplate.opsForValue().get(idempotencyKey);
        assertEquals(paymentId.toString(), storedValue);
    }
}
