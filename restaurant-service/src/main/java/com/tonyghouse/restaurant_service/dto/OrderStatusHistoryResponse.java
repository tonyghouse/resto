package com.tonyghouse.restaurant_service.dto;

import com.tonyghouse.restaurant_service.constants.OrderStatus;
import lombok.Data;

import java.time.Instant;

@Data
public class OrderStatusHistoryResponse {

    private OrderStatus oldStatus;
    private OrderStatus newStatus;
    private Instant changedAt;

    public OrderStatusHistoryResponse(
            OrderStatus oldStatus,
            OrderStatus newStatus,
            Instant changedAt) {
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedAt = changedAt;
    }
}
