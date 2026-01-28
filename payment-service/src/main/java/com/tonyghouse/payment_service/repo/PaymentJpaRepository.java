package com.tonyghouse.payment_service.repo;

import com.tonyghouse.payment_service.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentJpaRepository
        extends JpaRepository<Payment, UUID> {
}
