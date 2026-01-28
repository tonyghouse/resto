package com.tonyghouse.payment_service.proxy;

import com.tonyghouse.payment_service.constants.PaymentResult;
import com.tonyghouse.payment_service.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class MockPaymentGatewayProcessor implements PaymentGatewayProcessor {

    @Override
    public PaymentResult process(Payment payment) {

        // Simulate real-world behavior
        int hash = payment.getPaymentId().hashCode();

        if (hash % 5 == 0) {
            return PaymentResult.TIMEOUT;
        }
        if (hash % 2 == 0) {
            return PaymentResult.SUCCESS;
        }
        return PaymentResult.FAILURE;
    }
}
