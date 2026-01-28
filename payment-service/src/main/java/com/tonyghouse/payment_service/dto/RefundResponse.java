package com.tonyghouse.payment_service.dto;

import com.tonyghouse.payment_service.constants.RefundStatus;
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
