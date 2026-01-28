package com.tonyghouse.payment_service.service;

import com.tonyghouse.payment_service.dto.CreatePaymentRequest;
import com.tonyghouse.payment_service.entity.Payment;

import java.util.UUID;

public interface PaymentService {

    Payment createPayment(CreatePaymentRequest request, String idempotencyKey);

    Payment getPayment(UUID paymentId);

    Payment processPayment(UUID paymentId);
}
