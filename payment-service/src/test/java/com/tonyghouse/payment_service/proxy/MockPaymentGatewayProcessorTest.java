package com.tonyghouse.payment_service.proxy;

import com.tonyghouse.payment_service.constants.PaymentResult;
import com.tonyghouse.payment_service.entity.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.function.IntPredicate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MockPaymentGatewayProcessorTest {

    private MockPaymentGatewayProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new MockPaymentGatewayProcessor();
    }

    @Test
    void shouldReturnTimeout() {
        Payment payment = new Payment();
        payment.setPaymentId(findIdMatching(h -> h % 5 == 0));

        assertEquals(PaymentResult.TIMEOUT, processor.process(payment));
    }

    @Test
    void shouldReturnSuccess() {
        Payment payment = new Payment();
        payment.setPaymentId(findIdMatching(h -> h % 2 == 0 && h % 5 != 0));

        assertEquals(PaymentResult.SUCCESS, processor.process(payment));
    }

    @Test
    void shouldReturnFailure() {
        Payment payment = new Payment();
        payment.setPaymentId(findIdMatching(h -> h % 2 != 0));

        assertEquals(PaymentResult.FAILURE, processor.process(payment));
    }

    private UUID findIdMatching(IntPredicate condition) {
        for (int i = 0; i < 100_000; i++) {
            UUID candidate = UUID.randomUUID();
            if (condition.test(candidate.hashCode())) {
                return candidate;
            }
        }
        throw new IllegalStateException("Could not find matching UUID");
    }
}
