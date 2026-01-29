package com.tonyghouse.restaurant_service.controller;

import com.tonyghouse.restaurant_service.dto.CreateOrderRequest;
import com.tonyghouse.restaurant_service.dto.OrderResponse;
import com.tonyghouse.restaurant_service.dto.PricePreviewResponse;
import com.tonyghouse.restaurant_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @PostMapping("/price-preview")
    @PreAuthorize("hasAnyRole('STAFF', 'CUSTOMER')") //Not for Admin
    public PricePreviewResponse preview(@RequestBody CreateOrderRequest request) {
        return service.preview(request);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF', 'CUSTOMER')") //Not for Admin
    public OrderResponse create(@RequestBody CreateOrderRequest request) {
        return service.create(request);
    }


    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('STAFF', 'CUSTOMER')") //Not for Admin
    public OrderResponse get(@PathVariable UUID orderId) {
        return service.get(orderId);
    }
}
