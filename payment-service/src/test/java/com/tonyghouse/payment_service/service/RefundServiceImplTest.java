package com.tonyghouse.payment_service.service;

import com.tonyghouse.payment_service.constants.PaymentStatus;
import com.tonyghouse.payment_service.constants.RefundStatus;
import com.tonyghouse.payment_service.dto.RefundRequest;
import com.tonyghouse.payment_service.entity.Payment;
import com.tonyghouse.payment_service.entity.Refund;
import com.tonyghouse.payment_service.exception.RestoPaymentException;
import com.tonyghouse.payment_service.repo.PaymentRepository;
import com.tonyghouse.payment_service.repo.RefundRepository;
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
class RefundServiceImplTest {

    @Mock
    PaymentRepository paymentRepository;

    @Mock
    RefundRepository refundRepository;

    @Mock
    Clock clock;

    @InjectMocks
    RefundServiceImpl service;

    @BeforeEach
    void setup() {
        Mockito.when(clock.instant())
                .thenReturn(Instant.parse("2025-01-01T00:00:00Z"));
    }

    @Test
    void refund_success_partial() {
        UUID paymentId = UUID.randomUUID();

        Payment payment = new Payment();
        payment.setPaymentId(paymentId);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPayableAmount(new BigDecimal("100"));

        RefundRequest req = new RefundRequest();
        req.setAmount(new BigDecimal("40"));
        req.setReason("TEST");

        Mockito.when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));
        Mockito.when(refundRepository.sumRefundedAmount(paymentId))
                .thenReturn(BigDecimal.ZERO);
        Mockito.when(refundRepository.save(Mockito.any()))
                .thenAnswer(i -> i.getArgument(0));

        Refund refund = service.refund(paymentId, req);

        assertEquals(RefundStatus.SUCCESS, refund.getStatus());
        Mockito.verify(paymentRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void refund_success_full() {
        UUID paymentId = UUID.randomUUID();

        Payment payment = new Payment();
        payment.setPaymentId(paymentId);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPayableAmount(new BigDecimal("100"));

        RefundRequest req = new RefundRequest();
        req.setAmount(new BigDecimal("100"));
        req.setReason("FULL");

        Mockito.when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));
        Mockito.when(refundRepository.sumRefundedAmount(paymentId))
                .thenReturn(BigDecimal.ZERO);
        Mockito.when(refundRepository.save(Mockito.any()))
                .thenAnswer(i -> i.getArgument(0));

        Refund refund = service.refund(paymentId, req);

        assertEquals(PaymentStatus.REFUNDED, payment.getStatus());
        Mockito.verify(paymentRepository).save(payment);
    }

    @Test
    void refund_paymentNotSuccessful() {
        UUID paymentId = UUID.randomUUID();

        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.FAILED);

        Mockito.when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        RefundRequest req = new RefundRequest();
        req.setAmount(new BigDecimal("10"));

        assertThrows(RestoPaymentException.class,
                () -> service.refund(paymentId, req));
    }

    @Test
    void refund_exceedsAmount() {
        UUID paymentId = UUID.randomUUID();

        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPayableAmount(new BigDecimal("100"));

        RefundRequest req = new RefundRequest();
        req.setAmount(new BigDecimal("60"));

        Mockito.when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));
        Mockito.when(refundRepository.sumRefundedAmount(paymentId))
                .thenReturn(new BigDecimal("50"));

        assertThrows(RestoPaymentException.class,
                () -> service.refund(paymentId, req));
    }
}
