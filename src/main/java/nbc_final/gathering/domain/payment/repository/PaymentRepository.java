package nbc_final.gathering.domain.payment.repository;

import nbc_final.gathering.domain.payment.entity.PayStatus;
import nbc_final.gathering.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(String orderId);
    Optional<Payment> findByPaymentKey(String paymentKey);  // 새로운 메서드 추가
    boolean existsByOrderIdAndStatus(String orderId, PayStatus status);
    boolean existsByOrderId(String orderId); // 주문 ID로 존재 여부 확인
}

