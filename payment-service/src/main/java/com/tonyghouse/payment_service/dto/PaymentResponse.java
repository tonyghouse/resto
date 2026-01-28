package com.tonyghouse.payment_service.dto;

import com.tonyghouse.payment_service.constants.PaymentMethod;
import com.tonyghouse.payment_service.constants.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class PaymentResponse {

    private UUID paymentId;
    private UUID orderId;

    private PaymentMethod method;
    private PaymentStatus status;

    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private BigDecimal payableAmount;

    private String currency;

    private Instant createdAt;
    private Instant updatedAt;
}
