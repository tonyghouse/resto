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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
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
        log.debug("Price preview requested. itemsCount={}",
                request.getItems() != null ? request.getItems().size() : 0);
        return orderService.preview(request);
    }


    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF', 'CUSTOMER')") //Not for Admin
    public OrderResponse create(@RequestBody CreateOrderRequest request) {

        log.info("Creating order. itemsCount={}",
                request.getItems() != null ? request.getItems().size() : 0);
        OrderResponse response = orderService.create(request);
        log.info("Order created successfully. orderId={}, totalAmount={}",
                response.getOrderId(), response.getBreakdown() != null ? response.getBreakdown().getGrandTotal() : null);
        return response;
    }


    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('STAFF', 'CUSTOMER')") //Not for Admin
    public OrderResponse get(@PathVariable UUID orderId) {
        log.debug("Fetching order. orderId={}", orderId);
        return orderService.get(orderId);
    }


    @PostMapping("/{orderId}/accept")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public void accept(@PathVariable UUID orderId) {
        log.info("Order accepted. orderId={}", orderId);
        orderStateService.accept(orderId);
    }


    @PostMapping("/{orderId}/preparing")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public void preparing(@PathVariable UUID orderId) {
        log.info("Order moved to PREPARING. orderId={}", orderId);
        orderStateService.markPreparing(orderId);
    }


    @PostMapping("/{orderId}/ready")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public void ready(@PathVariable UUID orderId) {
        log.info("Order marked READY. orderId={}", orderId);
        orderStateService.markReady(orderId);
    }


    @PostMapping("/{orderId}/delivered")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public void delivered(@PathVariable UUID orderId) {
        log.info("Order delivered. orderId={}", orderId);
        orderStateService.markDelivered(orderId);
    }


    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("hasAnyRole('STAFF', 'CUSTOMER', 'ADMIN')")
    public void cancel(@PathVariable UUID orderId) {
        log.warn("Order cancelled. orderId={}", orderId);
        orderStateService.cancel(orderId);
    }


    @GetMapping("/{orderId}/status-history")
    @PreAuthorize("hasAnyRole('STAFF', 'CUSTOMER', 'ADMIN')")
    public List<OrderStatusHistoryResponse> history(
            @PathVariable UUID orderId) {
        log.debug("Fetching order status history. orderId={}", orderId);
        List<OrderStatusHistoryResponse> history = orderStateService.history(orderId);
        log.debug("Order history fetched. orderId={}, events={}", orderId, history.size());
        return history;
    }


    @PostMapping("/{orderId}/payments")
    @PreAuthorize("hasAnyRole('STAFF', 'CUSTOMER')") //Not for Admin. Staff collects money by hand and pay
    public void initiate(
            @PathVariable UUID orderId,
            @RequestBody InitiatePaymentRequest request) {
        log.info("Initiating payment. orderId={}", orderId);
        orderPaymentService.initiatePayment(orderId, request);
    }


    @PostMapping("/{orderId}/payments/retry")
    @PreAuthorize("hasAnyRole('STAFF', 'CUSTOMER')") //Not for Admin. Staff collects money by hand and pay
    public void retry(@PathVariable UUID orderId) {
        log.warn("Retrying payment. orderId={}", orderId);
        orderPaymentService.retryPayment(orderId);
    }


    @PostMapping("/{orderId}/refunds")
    @PreAuthorize("hasAnyRole('STAFF', 'CUSTOMER')") //Not for Admin. Staff collects money by hand and pay
    public void refund(
            @PathVariable UUID orderId,
            @RequestBody RefundRequestDto request) {
        log.warn("Refund requested. orderId={}, amount={}",
                orderId, request.getAmount());
        orderPaymentService.refund(orderId, request);
    }
}
