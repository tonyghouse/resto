package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.dto.OrderStatusHistoryResponse;

import java.util.List;
import java.util.UUID;

public interface OrderStateService {

    void accept(UUID orderId);

    void markPreparing(UUID orderId);

    void markReady(UUID orderId);

    void markDelivered(UUID orderId);

    void cancel(UUID orderId);

    List<OrderStatusHistoryResponse> history(UUID orderId);
}
