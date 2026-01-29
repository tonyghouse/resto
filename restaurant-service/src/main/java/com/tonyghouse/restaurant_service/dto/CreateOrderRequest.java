package com.tonyghouse.restaurant_service.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateOrderRequest {

    private UUID branchId;
    private String customerName;
    private String customerPhone;
    private List<OrderItemRequest> items;
}
