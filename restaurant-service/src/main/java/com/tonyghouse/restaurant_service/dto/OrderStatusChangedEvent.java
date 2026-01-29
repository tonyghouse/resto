package com.tonyghouse.restaurant_service.dto;

import com.tonyghouse.restaurant_service.constants.OrderStatus;
import java.time.Instant;
import java.util.UUID;

public record OrderStatusChangedEvent(
        UUID orderId,
        OrderStatus oldStatus,
        OrderStatus newStatus,
        Instant changedAt
) {}
