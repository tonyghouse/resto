package com.tonyghouse.payment_service.repo;

import com.tonyghouse.payment_service.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefundRepository extends JpaRepository<Refund, UUID> {

    @Query("""
            SELECT COALESCE(SUM(r.refundAmount), 0)
            FROM Refund r
            WHERE r.payment.paymentId = :paymentId
           """)
    Optional<BigDecimal> sumRefundedAmountRaw(@Param("paymentId") UUID paymentId);

    default BigDecimal sumRefundedAmount(UUID paymentId) {
        return sumRefundedAmountRaw(paymentId)
                .orElse(BigDecimal.ZERO);
    }
}
