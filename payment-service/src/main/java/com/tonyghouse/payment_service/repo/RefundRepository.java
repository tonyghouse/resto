package com.tonyghouse.payment_service.repo;

import com.tonyghouse.payment_service.entity.Refund;

import java.math.BigDecimal;
import java.util.UUID;

public interface RefundRepository {

    Refund save(Refund refund);

    BigDecimal sumRefundedAmount(UUID paymentId);
}
