package com.tonyghouse.restaurant_service.mapper;

import com.tonyghouse.restaurant_service.dto.OrderResponse;
import com.tonyghouse.restaurant_service.dto.PriceBreakdown;
import com.tonyghouse.restaurant_service.entity.Order;

public class OrderMapper {
    public static OrderResponse toResponse(Order order, PriceBreakdown breakdown) {
        OrderResponse r = new OrderResponse();
        r.setOrderId(order.getId());
        r.setStatus(order.getStatus().name());
        r.setBreakdown(breakdown);
        r.setCreatedAt(order.getCreatedAt());
        return r;
    }
}
