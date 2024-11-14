package nbc_final.gathering.domain.payment.repository;

import nbc_final.gathering.domain.payment.entity.Payment;
import nbc_final.gathering.domain.payment.entity.PayStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    boolean existsByOrderId(String orderId);
    Optional<Payment> findByOrderId(String orderId);
    boolean existsByOrderIdAndStatus(String orderId, PayStatus status);
    Optional<Payment> findByPaymentKey(String paymentKey);
}
