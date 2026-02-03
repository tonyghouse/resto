package com.tonyghouse.payment_service.repo;

import com.tonyghouse.payment_service.entity.IdempotencyKey;
import com.tonyghouse.payment_service.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IdempotencyKeyRepository
        extends JpaRepository<IdempotencyKey, Long> {

    Optional<IdempotencyKey> findByIdempotencyKey(String idempotencyKey);

    default Optional<UUID> findPaymentId(String key) {
        return findByIdempotencyKey(key)
                .map(i -> i.getPayment().getPaymentId());
    }

    default void save(String key, Payment savedPayment) {
        IdempotencyKey entity = new IdempotencyKey();
        entity.setIdempotencyKey(key);
        entity.setPayment(savedPayment);
        entity.setCreatedAt(Instant.now());
        save(entity);
    }
}
