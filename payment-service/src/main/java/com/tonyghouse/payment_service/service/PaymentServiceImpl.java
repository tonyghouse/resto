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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final PaymentGatewayProcessor paymentGatewayProcessor;
    private final Clock clock;

    @Override
    public Payment createPayment(CreatePaymentRequest request, String idempotencyKey) {
        log.info("Creating payment for orderId={} with idempotencyKey={}", request.getOrderId(), idempotencyKey);

        // Idempotency
        Optional<UUID> existingPaymentId =
                idempotencyKeyRepository.findPaymentId(idempotencyKey);

        if (existingPaymentId.isPresent()) {
            log.info("Idempotent request detected. Returning existing paymentId={}", existingPaymentId.get());
            return paymentRepository.findById(existingPaymentId.get())
                    .orElseThrow(() -> new IllegalStateException("Idempotent payment missing"));
        }
        validateAmounts(request);
        log.debug("Amounts validated for orderId={}", request.getOrderId());


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

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment created successfully. paymentId={}", savedPayment.getPaymentId());

        idempotencyKeyRepository.save(
                idempotencyKey,
                savedPayment
        );

        return payment;
    }


    private void validateAmounts(CreatePaymentRequest request) {

        if (request.getTotalAmount().signum() <= 0) {
            log.warn("Invalid total amount received: {}", request.getTotalAmount());
            throw new RestoPaymentException("Total amount must be positive", HttpStatus.BAD_REQUEST);
        }

    }

    @Override
    public Payment processPayment(UUID paymentId) {
        log.info("Processing paymentId={}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RestoPaymentException("Payment not found: "+paymentId, HttpStatus.INTERNAL_SERVER_ERROR));
        log.debug("Current status for paymentId={} is {}", paymentId, payment.getStatus());

        if (payment.getStatus() != PaymentStatus.INITIATED) {
            log.info("Payment already processed. Skipping. paymentId={} status={}", paymentId, payment.getStatus());
            return payment; // idempotent processing
        }

        payment.setStatus(PaymentStatus.PROCESSING);
        payment.setUpdatedAt(clock.instant());
        paymentRepository.save(payment);

        log.info("Calling payment gateway for paymentId={}", paymentId);
        PaymentResult result = paymentGatewayProcessor.process(payment);
        log.info("Gateway result for paymentId={} is {}", paymentId, result);

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
        log.info("Final status for paymentId={} is {}", paymentId, payment.getStatus());
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Payment getPayment(UUID paymentId) {
        log.debug("Fetching paymentId={}", paymentId);
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RestoPaymentException("Payment not found: "+paymentId, HttpStatus.INTERNAL_SERVER_ERROR));
    }


}
