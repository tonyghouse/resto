package com.tonyghouse.payment_service.repo;

import java.util.Optional;
import java.util.UUID;

public interface IdempotencyKeyRepository {

    Optional<UUID> findPaymentId(String key);

    void save(String key, UUID paymentId);
}
