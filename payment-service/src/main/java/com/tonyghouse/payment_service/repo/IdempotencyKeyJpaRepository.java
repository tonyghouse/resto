package com.tonyghouse.payment_service.repo;

import com.tonyghouse.payment_service.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyKeyJpaRepository
        extends JpaRepository<IdempotencyKey, Long> {

    Optional<IdempotencyKey> findByIdempotencyKey(String idempotencyKey);
}
