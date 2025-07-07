package org.example.fitpass.domain.payment.repository;

import java.util.List;
import java.util.Optional;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByOrderId(String orderId);

    default Payment findByIdOrElseThrow(String orderId) {
        Payment payment = findByOrderId(orderId).orElseThrow(
            () -> new BaseException(ExceptionCode.PAYMENT_NOT_FOUND));
        return payment;
    }
    
    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);
}
