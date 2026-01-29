package com.tonyghouse.restaurant_service.dto;

import com.tonyghouse.restaurant_service.constants.payment.PaymentMethod;
import lombok.Data;

@Data
public class InitiatePaymentRequest {

    private PaymentMethod method;
    private String currency;
}
