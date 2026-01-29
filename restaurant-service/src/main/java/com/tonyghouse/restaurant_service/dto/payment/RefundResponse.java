package com.tonyghouse.restaurant_service.dto.payment;

import com.tonyghouse.restaurant_service.constants.payment.RefundStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class RefundResponse {

    private UUID refundId;
    private UUID paymentId;

    private BigDecimal refundAmount;
    private RefundStatus status;

    private Instant createdAt;
}
