package com.tonyghouse.restaurant_service.dto;

import com.tonyghouse.restaurant_service.constants.payment.PaymentStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class PaymentCallbackRequest {

    private UUID paymentId;
    private PaymentStatus status;
}
