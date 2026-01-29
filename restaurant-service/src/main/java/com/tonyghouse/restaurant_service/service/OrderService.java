package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.dto.CreateOrderRequest;
import com.tonyghouse.restaurant_service.dto.OrderResponse;
import com.tonyghouse.restaurant_service.dto.PricePreviewResponse;

import java.util.UUID;

public interface OrderService {

    OrderResponse create(CreateOrderRequest request);

    OrderResponse get(UUID orderId);

    PricePreviewResponse preview(CreateOrderRequest request);
}
