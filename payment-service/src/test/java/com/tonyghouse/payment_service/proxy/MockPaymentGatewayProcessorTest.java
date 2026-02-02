package com.tonyghouse.payment_service.proxy;

import com.tonyghouse.payment_service.constants.PaymentResult;
import com.tonyghouse.payment_service.entity.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("Should return TIMEOUT when hash divisible by 5")
    void shouldReturnTimeout() {
        Payment payment = new Payment();
        payment.setPaymentId(findIdMatching(h -> h % 5 == 0));

        assertEquals(PaymentResult.TIMEOUT, processor.process(payment));
    }

    @Test
    @DisplayName("Should return SUCCESS when hash divisible by 2 but not 5")
    void shouldReturnSuccess() {
        Payment payment = new Payment();
        payment.setPaymentId(findIdMatching(h -> h % 2 == 0 && h % 5 != 0));

        assertEquals(PaymentResult.SUCCESS, processor.process(payment));
    }

    @Test
    @DisplayName("Should return FAILURE when hash is odd")
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
