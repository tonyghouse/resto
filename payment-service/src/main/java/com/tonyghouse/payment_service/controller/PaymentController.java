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

        Payment payment = paymentService.createPayment(request, idempotencyKey);
        CreatePaymentResponse createPaymentResponse = PaymentMapper.mapToCreatePaymentResponse(payment);
        return new ResponseEntity<>(createPaymentResponse, HttpStatus.CREATED);
    }

    @PostMapping("/{paymentId}/process")
    @PreAuthorize("hasRole('RESTAURANT_SERVICE')")
    public ResponseEntity<PaymentResponse> processPayment(
            @PathVariable UUID paymentId
    ) {
        Payment payment = paymentService.processPayment(paymentId);
        return ResponseEntity.ok(PaymentMapper.toPaymentResponse(payment));
    }


    @GetMapping("/{paymentId}")
    @PreAuthorize("hasRole('RESTAURANT_SERVICE')")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID paymentId) {
        Payment payment = paymentService.getPayment(paymentId);
        return ResponseEntity.ok(PaymentMapper.toPaymentResponse(payment));
    }

    @PostMapping("/{paymentId}/refund")
    @PreAuthorize("hasRole('RESTAURANT_SERVICE')")
    public ResponseEntity<RefundResponse> refund(@PathVariable UUID paymentId, @Valid @RequestBody RefundRequest request) {
        Refund refund = refundService.refund(paymentId, request);
        return ResponseEntity.ok(PaymentMapper.toRefundResponse(refund));
    }
}