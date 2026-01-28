package com.tonyghouse.payment_service.service;

import com.tonyghouse.payment_service.constants.PaymentStatus;
import com.tonyghouse.payment_service.constants.RefundStatus;
import com.tonyghouse.payment_service.dto.RefundRequest;
import com.tonyghouse.payment_service.entity.Payment;
import com.tonyghouse.payment_service.entity.Refund;
import com.tonyghouse.payment_service.exception.InvalidRefundException;
import com.tonyghouse.payment_service.exception.PaymentNotFoundException;
import com.tonyghouse.payment_service.repo.PaymentRepository;
import com.tonyghouse.payment_service.repo.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RefundServiceImpl implements RefundService {

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final Clock clock;

    @Override
    public Refund refund(UUID paymentId, RefundRequest request) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        // ---------- State Guard ----------
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new InvalidRefundException("Payment not successful");
        }

        BigDecimal totalRefunded =
                refundRepository.sumRefundedAmount(paymentId);

        BigDecimal newTotal =
                totalRefunded.add(request.getAmount());

        if (newTotal.compareTo(payment.getPayableAmount()) > 0) {
            throw new InvalidRefundException("Refund exceeds paid amount");
        }

        Refund refund = new Refund();
        refund.setRefundId(UUID.randomUUID());
        refund.setPaymentId(paymentId);
        refund.setRefundAmount(request.getAmount());
        refund.setReason(request.getReason());
        refund.setStatus(RefundStatus.SUCCESS);
        refund.setCreatedAt(clock.instant());

        refundRepository.save(refund);

        // ---------- Final Refund State ----------
        if (newTotal.compareTo(payment.getPayableAmount()) == 0) {
            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setUpdatedAt(clock.instant());
            paymentRepository.save(payment);
        }

        return refund;
    }
}
