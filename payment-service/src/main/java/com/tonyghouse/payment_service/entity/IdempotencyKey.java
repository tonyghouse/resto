package com.tonyghouse.payment_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "idempotency_key",
        uniqueConstraints = @UniqueConstraint(columnNames = "idempotency_key")
)
@Getter
@Setter
public class IdempotencyKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "idempotency_key_seq")
    @SequenceGenerator(
            name = "idempotency_key_seq",
            sequenceName = "idempotency_key_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    @Column(nullable = false)
    private UUID paymentId;

    private Instant createdAt;
}
