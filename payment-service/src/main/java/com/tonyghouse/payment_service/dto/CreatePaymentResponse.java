package com.tonyghouse.payment_service.dto;

import com.tonyghouse.payment_service.constants.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class CreatePaymentResponse {

    private UUID paymentId;
    private UUID orderId;
    private PaymentStatus status;

    private BigDecimal payableAmount;
    private String currency;

    private Instant createdAt;
}
