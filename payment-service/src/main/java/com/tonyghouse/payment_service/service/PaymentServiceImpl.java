package com.tonyghouse.payment_service.service;

import com.tonyghouse.payment_service.constants.PaymentStatus;
import com.tonyghouse.payment_service.dto.CreatePaymentRequest;
import com.tonyghouse.payment_service.entity.Payment;
import com.tonyghouse.payment_service.exception.InvalidPaymentException;
import com.tonyghouse.payment_service.exception.PaymentNotFoundException;
import com.tonyghouse.payment_service.repo.IdempotencyKeyRepository;
import com.tonyghouse.payment_service.repo.PaymentRepository;
import lombok.RequiredArgsConstructor;
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

    @Override
    @Transactional(readOnly = true)
    public Payment getPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
    }

    private void validateAmounts(CreatePaymentRequest request) {

        if (request.getPayableAmount()
                .compareTo(request.getTotalAmount().add(request.getTaxAmount())) != 0) {
            throw new InvalidPaymentException("Payable amount mismatch");
        }

        if (request.getTotalAmount().signum() <= 0) {
            throw new InvalidPaymentException("Total amount must be positive");
        }
    }
}
