package com.tonyghouse.payment_service.service;

import com.tonyghouse.payment_service.constants.PaymentResult;
import com.tonyghouse.payment_service.constants.PaymentStatus;
import com.tonyghouse.payment_service.dto.CreatePaymentRequest;
import com.tonyghouse.payment_service.entity.Payment;
import com.tonyghouse.payment_service.exception.RestoPaymentException;
import com.tonyghouse.payment_service.proxy.PaymentGatewayProcessor;
import com.tonyghouse.payment_service.repo.IdempotencyKeyRepository;
import com.tonyghouse.payment_service.repo.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaymentServiceImplTest {

    @Mock
    PaymentRepository paymentRepository;

    @Mock
    IdempotencyKeyRepository idempotencyKeyRepository;

    @Mock
    PaymentGatewayProcessor paymentGatewayProcessor;

    @Mock
    Clock clock;

    @InjectMocks
    PaymentServiceImpl service;

    @BeforeEach
    void setup() {
        Mockito.when(clock.instant())
                .thenReturn(Instant.parse("2025-01-01T00:00:00Z"));
    }

    @Test
    void createPayment_success() {
        UUID orderId = UUID.randomUUID();

        CreatePaymentRequest req = new CreatePaymentRequest();
        req.setOrderId(orderId);
        req.setTotalAmount(new BigDecimal("100"));
        req.setTaxAmount(new BigDecimal("10"));
        req.setPayableAmount(new BigDecimal("110"));

        Mockito.when(idempotencyKeyRepository.findPaymentId("key"))
                .thenReturn(Optional.empty());

        Mockito.when(paymentRepository.save(Mockito.any(Payment.class)))
                .thenAnswer(i -> i.getArgument(0)); // returning same object

        Payment payment = service.createPayment(req, "key");

        assertEquals(PaymentStatus.INITIATED, payment.getStatus());

        Mockito.verify(idempotencyKeyRepository)
                .save(Mockito.eq("key"), Mockito.any(Payment.class));
    }


    @Test
    void createPayment_idempotent() {
        UUID paymentId = UUID.randomUUID();
        Payment existing = new Payment();
        existing.setPaymentId(paymentId);

        Mockito.when(idempotencyKeyRepository.findPaymentId("key"))
                .thenReturn(Optional.of(paymentId));
        Mockito.when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(existing));

        Payment payment =
                service.createPayment(new CreatePaymentRequest(), "key");

        assertEquals(paymentId, payment.getPaymentId());
    }

    @Test
    void createPayment_invalidAmount() {
        CreatePaymentRequest req = new CreatePaymentRequest();
        req.setTotalAmount(BigDecimal.ZERO);

        Mockito.when(idempotencyKeyRepository.findPaymentId("key"))
                .thenReturn(Optional.empty());

        assertThrows(RestoPaymentException.class,
                () -> service.createPayment(req, "key"));
    }



    @Test
    void processPayment_idempotent() {
        UUID paymentId = UUID.randomUUID();
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.SUCCESS);

        Mockito.when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        Payment res = service.processPayment(paymentId);

        assertEquals(PaymentStatus.SUCCESS, res.getStatus());
        Mockito.verify(paymentGatewayProcessor, Mockito.never())
                .process(Mockito.any());
    }

    @Test
    void processPayment_notFound_throwsException() {
        UUID paymentId = UUID.randomUUID();

        Mockito.when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.empty());

        assertThrows(RestoPaymentException.class,
                () -> service.processPayment(paymentId));

        Mockito.verify(paymentGatewayProcessor, Mockito.never())
                .process(Mockito.any());
    }

    @Test
    void processPayment_idempotent_noProcessing() {
        UUID paymentId = UUID.randomUUID();

        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.SUCCESS);

        Mockito.when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        Payment res = service.processPayment(paymentId);

        assertEquals(PaymentStatus.SUCCESS, res.getStatus());

        Mockito.verify(paymentGatewayProcessor, Mockito.never())
                .process(Mockito.any());
        Mockito.verify(paymentRepository, Mockito.never())
                .save(Mockito.any());
    }

    @Test
    void processPayment_success() {
        UUID paymentId = UUID.randomUUID();

        Payment payment = new Payment();
        payment.setPaymentId(paymentId);
        payment.setStatus(PaymentStatus.INITIATED);

        Mockito.when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        Mockito.when(paymentRepository.save(Mockito.any()))
                .thenAnswer(i -> i.getArgument(0));

        Mockito.when(paymentGatewayProcessor.process(payment))
                .thenReturn(PaymentResult.SUCCESS);

        Payment res = service.processPayment(paymentId);

        assertEquals(PaymentStatus.SUCCESS, res.getStatus());
        assertEquals(Instant.parse("2025-01-01T00:00:00Z"), res.getUpdatedAt());

        Mockito.verify(paymentRepository, Mockito.times(2)).save(Mockito.any());
    }


    @Test
    void processPayment_failure() {
        UUID paymentId = UUID.randomUUID();

        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.INITIATED);

        Mockito.when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        Mockito.when(paymentRepository.save(Mockito.any()))
                .thenAnswer(i -> i.getArgument(0));

        Mockito.when(paymentGatewayProcessor.process(payment))
                .thenReturn(PaymentResult.FAILURE);

        Payment res = service.processPayment(paymentId);

        assertEquals(PaymentStatus.FAILED, res.getStatus());

        Mockito.verify(paymentRepository, Mockito.times(2)).save(Mockito.any());
    }

    @Test
    void processPayment_timeout_shouldRetryAndIncrementCounter() {
        UUID paymentId = UUID.randomUUID();

        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.INITIATED);
        payment.setRetryCount(1);

        Mockito.when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        Mockito.when(paymentRepository.save(Mockito.any()))
                .thenAnswer(i -> i.getArgument(0));

        Mockito.when(paymentGatewayProcessor.process(payment))
                .thenReturn(PaymentResult.TIMEOUT);

        Payment res = service.processPayment(paymentId);

        assertEquals(PaymentStatus.RETRYING, res.getStatus());
        assertEquals(2, res.getRetryCount());

        Mockito.verify(paymentRepository, Mockito.times(2)).save(Mockito.any());
    }






    @Test
    void getPayment_notFound() {
        Mockito.when(paymentRepository.findById(Mockito.any()))
                .thenReturn(Optional.empty());

        assertThrows(RestoPaymentException.class,
                () -> service.getPayment(UUID.randomUUID()));
    }
}
