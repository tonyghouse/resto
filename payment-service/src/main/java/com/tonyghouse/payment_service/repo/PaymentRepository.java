package com.tonyghouse.payment_service.repo;

import com.tonyghouse.payment_service.entity.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {

    Optional<Payment> findById(UUID paymentId);

    Payment save(Payment payment);
}
