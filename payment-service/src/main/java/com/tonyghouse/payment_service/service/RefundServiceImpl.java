package com.tonyghouse.payment_service.service;

import com.tonyghouse.payment_service.constants.PaymentStatus;
import com.tonyghouse.payment_service.constants.RefundStatus;
import com.tonyghouse.payment_service.dto.RefundRequest;
import com.tonyghouse.payment_service.entity.Payment;
import com.tonyghouse.payment_service.entity.Refund;
import com.tonyghouse.payment_service.exception.RestoPaymentException;
import com.tonyghouse.payment_service.repo.PaymentRepository;
import com.tonyghouse.payment_service.repo.RefundRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RefundServiceImpl implements RefundService {

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final Clock clock;

    @Override
    public Refund refund(UUID paymentId, RefundRequest request) {
        log.info("Refund requested. paymentId={} amount={}", paymentId, request.getAmount());

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RestoPaymentException("Payment not found: "+paymentId, HttpStatus.INTERNAL_SERVER_ERROR));

        log.debug("Payment found. paymentId={} status={} payableAmount={}",
                paymentId, payment.getStatus(), payment.getPayableAmount());

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            log.warn("Refund rejected. Payment not successful. paymentId={} status={}", paymentId, payment.getStatus());
            throw new RestoPaymentException("Payment not successful", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        BigDecimal totalRefunded =
                refundRepository.sumRefundedAmount(paymentId);

        BigDecimal newTotal =
                totalRefunded.add(request.getAmount());
        log.debug("Refund calculation. requested={} newTotal={} payable={}",
                request.getAmount(), newTotal, payment.getPayableAmount());

        if (newTotal.compareTo(payment.getPayableAmount()) > 0) {
            log.warn("Refund exceeds payable amount. paymentId={} newTotal={} payable={}",
                    paymentId, newTotal, payment.getPayableAmount());
            throw new RestoPaymentException("Refund exceeds paid amount", HttpStatus.BAD_REQUEST);
        }

        Refund refund = new Refund();
        refund.setRefundId(UUID.randomUUID());
        refund.setPayment(payment);
        refund.setRefundAmount(request.getAmount());
        refund.setReason(request.getReason());
        refund.setStatus(RefundStatus.SUCCESS);
        refund.setCreatedAt(clock.instant());

        log.info("Creating refund. refundId={} paymentId={} amount={} reason={}",
                refund.getRefundId(), paymentId, refund.getRefundAmount(), refund.getReason());
        refundRepository.save(refund);
        log.info("Refund saved successfully. refundId={}", refund.getRefundId());


        if (newTotal.compareTo(payment.getPayableAmount()) == 0) {
            log.info("Payment fully refunded. Updating status to REFUNDED. paymentId={}", paymentId);
            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setUpdatedAt(clock.instant());
            paymentRepository.save(payment);
        }

        return refund;
    }
}
