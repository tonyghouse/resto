package com.tonyghouse.payment_service.service;

import com.tonyghouse.payment_service.constants.PaymentResult;
import com.tonyghouse.payment_service.constants.PaymentStatus;
import com.tonyghouse.payment_service.dto.CreatePaymentRequest;
import com.tonyghouse.payment_service.entity.Payment;
import com.tonyghouse.payment_service.exception.RestoPaymentException;
import com.tonyghouse.payment_service.proxy.PaymentGatewayProcessor;
import com.tonyghouse.payment_service.repo.IdempotencyKeyRepository;
import com.tonyghouse.payment_service.repo.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final PaymentGatewayProcessor paymentGatewayProcessor;
    private final Clock clock;

    @Override
    public Payment createPayment(CreatePaymentRequest request, String idempotencyKey) {

        // ---------- Idempotency ----------
        Optional<UUID> existingPaymentId =
                idempotencyKeyRepository.findPaymentId(idempotencyKey);

        if (existingPaymentId.isPresent()) {
            return paymentRepository.findById(existingPaymentId.get())
                    .orElseThrow(() -> new IllegalStateException("Idempotent payment missing"));
        }

        // ---------- Validation ----------
        validateAmounts(request);

        Payment payment = new Payment();
        payment.setPaymentId(UUID.randomUUID());
        payment.setOrderId(request.getOrderId());
        payment.setMethod(request.getMethod());
        payment.setCurrency(request.getCurrency());

        payment.setTotalAmount(request.getTotalAmount());
        payment.setTaxAmount(request.getTaxAmount());
        payment.setPayableAmount(request.getPayableAmount());

        payment.setStatus(PaymentStatus.INITIATED);
        payment.setRetryCount(0);
        payment.setCreatedAt(clock.instant());
        payment.setUpdatedAt(clock.instant());

        paymentRepository.save(payment);

        idempotencyKeyRepository.save(
                idempotencyKey,
                payment.getPaymentId()
        );

        return payment;
    }


    private void validateAmounts(CreatePaymentRequest request) {

        if (request.getPayableAmount()
                .compareTo(request.getTotalAmount().add(request.getTaxAmount())) != 0) {
            throw new RestoPaymentException("Payable amount mismatch", HttpStatus.BAD_REQUEST);
        }

        if (request.getTotalAmount().signum() <= 0) {
            throw new RestoPaymentException("Total amount must be positive", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public Payment processPayment(UUID paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RestoPaymentException("Payment not found: "+paymentId, HttpStatus.INTERNAL_SERVER_ERROR));

        if (payment.getStatus() != PaymentStatus.INITIATED) {
            return payment; // idempotent processing
        }

        payment.setStatus(PaymentStatus.PROCESSING);
        payment.setUpdatedAt(clock.instant());
        paymentRepository.save(payment);

        PaymentResult result = paymentGatewayProcessor.process(payment);

        switch (result) {
            case SUCCESS -> {
                payment.setStatus(PaymentStatus.SUCCESS);
            }
            case FAILURE -> {
                payment.setStatus(PaymentStatus.FAILED);
            }
            case TIMEOUT -> {
                payment.setStatus(PaymentStatus.RETRYING);
                payment.setRetryCount(payment.getRetryCount() + 1);
            }
        }

        payment.setUpdatedAt(clock.instant());
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Payment getPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RestoPaymentException("Payment not found: "+paymentId, HttpStatus.INTERNAL_SERVER_ERROR));
    }


}
