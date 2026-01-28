package com.tonyghouse.payment_service.repo;

import com.tonyghouse.payment_service.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface RefundJpaRepository
        extends JpaRepository<Refund, UUID> {

    @Query("""
                SELECT COALESCE(SUM(r.refundAmount), 0)
                FROM Refund r
                WHERE r.paymentId = :paymentId
            """)
    Optional<BigDecimal> sumRefundedAmount(@Param("paymentId") UUID paymentId);
}
