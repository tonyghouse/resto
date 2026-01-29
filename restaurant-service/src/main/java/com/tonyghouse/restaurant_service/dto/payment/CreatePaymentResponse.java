package com.tonyghouse.restaurant_service.dto.payment;

import com.tonyghouse.restaurant_service.constants.payment.PaymentStatus;
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
