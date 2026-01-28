package com.tonyghouse.payment_service.entity;

import com.tonyghouse.payment_service.constants.RefundStatus;
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
@Table(name = "refund")
@Getter
@Setter
public class Refund {

    @Id
    private UUID refundId;

    private UUID paymentId;

    private BigDecimal refundAmount;

    @Enumerated(EnumType.STRING)
    private RefundStatus status;

    private String reason;

    private Instant createdAt;
}

