package com.tonyghouse.restaurant_service.controller;

import com.tonyghouse.restaurant_service.dto.CreateOrderRequest;
import com.tonyghouse.restaurant_service.dto.InitiatePaymentRequest;
import com.tonyghouse.restaurant_service.dto.OrderResponse;
import com.tonyghouse.restaurant_service.dto.OrderStatusHistoryResponse;
import com.tonyghouse.restaurant_service.dto.PaymentCallbackRequest;
import com.tonyghouse.restaurant_service.dto.PricePreviewResponse;
import com.tonyghouse.restaurant_service.dto.RefundRequestDto;
import com.tonyghouse.restaurant_service.service.OrderPaymentService;
import com.tonyghouse.restaurant_service.service.OrderService;
import com.tonyghouse.restaurant_service.service.OrderStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderStateService orderStateService;
    private final OrderPaymentService orderPaymentService;

    @PostMapping("/price-preview")
    @PreAuthorize("hasAnyRole('STAFF', 'CUSTOMER')") //Not for Admin
    public PricePreviewResponse preview(@RequestBody CreateOrderRequest request) {
        return orderService.preview(request);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF', 'CUSTOMER')") //Not for Admin
    public OrderResponse create(@RequestBody CreateOrderRequest request) {
        return orderService.create(request);
    }


    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('STAFF', 'CUSTOMER')") //Not for Admin
    public OrderResponse get(@PathVariable UUID orderId) {
        return orderService.get(orderId);
    }

    @PostMapping("/{orderId}/accept")
    @PreAuthorize("hasAnyRole('STAFF', 'CUSTOMER')") //Not for Admin
    public void accept(@PathVariable UUID orderId) {
        orderStateService.accept(orderId);
    }

    @PostMapping("/{orderId}/preparing")
    @PreAuthorize("hasAnyRole('STAFF', 'CUSTOMER')") //Not for Admin
    public void preparing(@PathVariable UUID orderId) {
        orderStateService.markPreparing(orderId);
    }

    @PostMapping("/{orderId}/ready")
    @PreAuthorize("hasAnyRole('STAFF', 'CUSTOMER')") //Not for Admin
    public void ready(@PathVariable UUID orderId) {
        orderStateService.markReady(orderId);
    }

    @PostMapping("/{orderId}/delivered")
    @PreAuthorize("hasAnyRole('STAFF', 'CUSTOMER')") //Not for Admin
    public void delivered(@PathVariable UUID orderId) {
        orderStateService.markDelivered(orderId);
    }

    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("hasAnyRole('STAFF', 'CUSTOMER')") //Not for Admin
    public void cancel(@PathVariable UUID orderId) {
        orderStateService.cancel(orderId);
    }

    @GetMapping("/{orderId}/status-history")
    @PreAuthorize("hasAnyRole('STAFF', 'CUSTOMER')") //Not for Admin
    public List<OrderStatusHistoryResponse> history(
            @PathVariable UUID orderId) {
        return orderStateService.history(orderId);
    }

    @PostMapping("/{orderId}/payments")
    @PreAuthorize("hasAnyRole('STAFF', 'CUSTOMER')") //Not for Admin. Staff collects money by hand and pay
    public void initiate(
            @PathVariable UUID orderId,
            @RequestBody InitiatePaymentRequest request) {
        orderPaymentService.initiatePayment(orderId, request);
    }

    @PostMapping("/{orderId}/payments/callback")
    @PreAuthorize("hasAnyRole('STAFF', 'CUSTOMER')") //Not for Admin. Staff collects money by hand and pay
    public void callback(
            @PathVariable UUID orderId,
            @RequestBody PaymentCallbackRequest request) {
        orderPaymentService.handleCallback(orderId, request);
    }

    @PostMapping("/{orderId}/payments/retry")
    @PreAuthorize("hasAnyRole('STAFF', 'CUSTOMER')") //Not for Admin. Staff collects money by hand and pay
    public void retry(@PathVariable UUID orderId) {
        orderPaymentService.retryPayment(orderId);
    }

    @PostMapping("/{orderId}/refunds")
    @PreAuthorize("hasAnyRole('STAFF', 'CUSTOMER')") //Not for Admin. Staff collects money by hand and pay
    public void refund(
            @PathVariable UUID orderId,
            @RequestBody RefundRequestDto request) {
        orderPaymentService.refund(orderId, request);
    }
}
