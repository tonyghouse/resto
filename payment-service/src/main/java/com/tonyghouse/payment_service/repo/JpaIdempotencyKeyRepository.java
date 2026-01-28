package com.tonyghouse.payment_service.repo;

import com.tonyghouse.payment_service.entity.IdempotencyKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaIdempotencyKeyRepository implements IdempotencyKeyRepository {

    private final IdempotencyKeyJpaRepository jpaRepository;
    private final Clock clock;

    @Override
    public Optional<UUID> findPaymentId(String key) {
        return jpaRepository.findByIdempotencyKey(key)
                .map(IdempotencyKey::getPaymentId);
    }

    @Override
    public void save(String key, UUID paymentId) {
        IdempotencyKey entity = new IdempotencyKey();
        entity.setIdempotencyKey(key);
        entity.setPaymentId(paymentId);
        entity.setCreatedAt(clock.instant());

        jpaRepository.save(entity);
    }
}
