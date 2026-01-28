package com.tonyghouse.payment_service.repo;

import com.tonyghouse.payment_service.entity.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaPaymentRepository implements PaymentRepository {

    private final PaymentJpaRepository jpaRepository;

    @Override
    public Optional<Payment> findById(UUID paymentId) {
        return jpaRepository.findById(paymentId);
    }

    @Override
    public Payment save(Payment payment) {
        return jpaRepository.save(payment);
    }
}
