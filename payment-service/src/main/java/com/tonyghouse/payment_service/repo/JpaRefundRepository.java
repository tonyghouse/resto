package com.tonyghouse.payment_service.repo;

import com.tonyghouse.payment_service.entity.Refund;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaRefundRepository implements RefundRepository {

    private final RefundJpaRepository jpaRepository;

    @Override
    public Refund save(Refund refund) {
        return jpaRepository.save(refund);
    }

    @Override
    public BigDecimal sumRefundedAmount(UUID paymentId) {
        return jpaRepository.sumRefundedAmount(paymentId)
                .orElse(BigDecimal.ZERO);
    }
}
