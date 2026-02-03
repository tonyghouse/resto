package com.tonyghouse.payment_service.controller;

import com.tonyghouse.payment_service.dto.CreatePaymentRequest;
import com.tonyghouse.payment_service.dto.CreatePaymentResponse;
import com.tonyghouse.payment_service.dto.PaymentResponse;
import com.tonyghouse.payment_service.dto.RefundRequest;
import com.tonyghouse.payment_service.dto.RefundResponse;
import com.tonyghouse.payment_service.entity.Payment;
import com.tonyghouse.payment_service.entity.Refund;
import com.tonyghouse.payment_service.mapper.PaymentMapper;
import com.tonyghouse.payment_service.service.PaymentService;
import com.tonyghouse.payment_service.service.RefundService;
import jakarta.validation.Valid;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final RefundService refundService;

    @PostMapping
    @PreAuthorize("hasRole('RESTAURANT_SERVICE')")
    public ResponseEntity<CreatePaymentResponse> createPayment(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreatePaymentRequest request) {

        log.info("Create payment request received. idempotencyKey={}", idempotencyKey);
        log.debug("CreatePaymentRequest payload={}", request);

        Payment payment = paymentService.createPayment(request, idempotencyKey);

        log.info("Payment created successfully. paymentId={}, status={}",
                payment.getPaymentId(), payment.getStatus());

        CreatePaymentResponse response =
                PaymentMapper.mapToCreatePaymentResponse(payment);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @PostMapping("/{paymentId}/process")
    @PreAuthorize("hasRole('RESTAURANT_SERVICE')")
    public ResponseEntity<PaymentResponse> processPayment(@PathVariable UUID paymentId) {

        log.info("Processing payment. paymentId={}", paymentId);

        Payment payment = paymentService.processPayment(paymentId);

        log.info("Payment processed. paymentId={}, status={}",
                paymentId, payment.getStatus());

        return ResponseEntity.ok(PaymentMapper.toPaymentResponse(payment));
    }


    @GetMapping("/{paymentId}")
    @PreAuthorize("hasRole('RESTAURANT_SERVICE')")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID paymentId) {

        log.debug("Fetching payment details. paymentId={}", paymentId);

        Payment payment = paymentService.getPayment(paymentId);

        return ResponseEntity.ok(PaymentMapper.toPaymentResponse(payment));
    }


    @PostMapping("/{paymentId}/refund")
    @PreAuthorize("hasRole('RESTAURANT_SERVICE')")
    public ResponseEntity<RefundResponse> refund(
            @PathVariable UUID paymentId,
            @Valid @RequestBody RefundRequest request) {

        log.info("Refund request received. paymentId={}", paymentId);
        log.debug("RefundRequest payload={}", request);

        Refund refund = refundService.refund(paymentId, request);

        log.info("Refund completed. refundId={}, paymentId={}",
                refund.getRefundId(), paymentId);

        return ResponseEntity.ok(PaymentMapper.toRefundResponse(refund, paymentId));
    }
}
