package com.tonyghouse.payment_service.repo;

import com.tonyghouse.payment_service.entity.IdempotencyKey;
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
                .map(IdempotencyKey::getPaymentId);
    }

    default void save(String key, UUID paymentId) {
        IdempotencyKey entity = new IdempotencyKey();
        entity.setIdempotencyKey(key);
        entity.setPaymentId(paymentId);
        entity.setCreatedAt(Instant.now());
        save(entity);
    }
}
