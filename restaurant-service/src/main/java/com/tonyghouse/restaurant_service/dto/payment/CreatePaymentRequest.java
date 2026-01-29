package com.tonyghouse.restaurant_service.dto.payment;

import com.tonyghouse.restaurant_service.constants.payment.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
