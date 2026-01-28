package com.tonyghouse.payment_service.dto;

import com.tonyghouse.payment_service.constants.PaymentMethod;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreatePaymentRequest {

    @NotNull
    private UUID orderId;

    @NotNull
    private PaymentMethod method;

    @NotBlank
    private String currency;

    /**
     * Final amounts calculated by Order service
     * Payment service treats this as immutable snapshot
     */
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal totalAmount;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal taxAmount;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal payableAmount;

}
