package com.tonyghouse.restaurant_service.dto;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class OrderResponse {

    private UUID orderId;
    private String status;
    private PriceBreakdown breakdown;
    private Instant createdAt;
}
