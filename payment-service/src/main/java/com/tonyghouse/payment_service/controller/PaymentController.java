package com.tonyghouse.payment_service.controller;

import com.tonyghouse.payment_service.constants.PaymentStatus;
import com.tonyghouse.payment_service.dto.CreatePaymentRequest;
import com.tonyghouse.payment_service.dto.CreatePaymentResponse;
import com.tonyghouse.payment_service.dto.PaymentResponse;
import com.tonyghouse.payment_service.dto.RefundRequest;
import com.tonyghouse.payment_service.dto.RefundResponse;
import com.tonyghouse.payment_service.entity.Payment;
import com.tonyghouse.payment_service.service.PaymentService;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.Instant;
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

    @PostMapping
    //@PreAuthorize("hasRole('RESTAURANT_SERVICE')")
    public ResponseEntity<CreatePaymentResponse> createPayment(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreatePaymentRequest request) {
        Payment payment = paymentService.createPayment(request, idempotencyKey);
        CreatePaymentResponse createPaymentResponse = new CreatePaymentResponse();
        createPaymentResponse.setPaymentId(payment.getPaymentId());
        createPaymentResponse.setOrderId(payment.getOrderId());
        createPaymentResponse.setStatus(payment.getStatus());
        createPaymentResponse.setPayableAmount(payment.getPayableAmount());
        createPaymentResponse.setCurrency(payment.getCurrency());
        createPaymentResponse.setCreatedAt(payment.getCreatedAt());

        return new ResponseEntity<>(createPaymentResponse, HttpStatus.CREATED);
    }

    @GetMapping("/{paymentId}")
    //@PreAuthorize("hasRole('RESTAURANT_SERVICE')")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID paymentId) {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{paymentId}/refund")
    //@PreAuthorize("hasRole('RESTAURANT_SERVICE')")
    public ResponseEntity<RefundResponse> refund(@PathVariable UUID paymentId, @Valid @RequestBody RefundRequest request) {
        return ResponseEntity.ok().build();
    }
}