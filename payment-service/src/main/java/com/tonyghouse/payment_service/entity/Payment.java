package com.tonyghouse.payment_service.entity;

import com.tonyghouse.payment_service.constants.PaymentMethod;
import com.tonyghouse.payment_service.constants.PaymentStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment")
@Getter
@Setter
public class Payment {

    @Id
    private UUID paymentId;

    private UUID orderId;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private BigDecimal payableAmount;

    private String currency;

    private int retryCount;

    private Instant createdAt;
    private Instant updatedAt;
}
