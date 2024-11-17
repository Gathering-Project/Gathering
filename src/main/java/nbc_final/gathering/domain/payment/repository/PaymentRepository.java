package nbc_final.gathering.domain.payment.repository;

import nbc_final.gathering.domain.payment.entity.PayStatus;
import nbc_final.gathering.domain.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByOrderIdAndStatus(String orderId, PayStatus status);

    Optional<Payment> findByOrderId(String orderId);

    Optional<Payment> findByPaymentKey(String paymentKey);

    Optional<Payment> findByAd_AdId(Long adId);

    Page<Payment> findAllByGathering_UserId(Long userId, Pageable pageable);


}
