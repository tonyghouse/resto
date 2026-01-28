package com.tonyghouse.payment_service.proxy;

import com.tonyghouse.payment_service.constants.PaymentResult;
import com.tonyghouse.payment_service.entity.Payment;

public interface PaymentGatewayProcessor {
    PaymentResult process(Payment payment);
}
