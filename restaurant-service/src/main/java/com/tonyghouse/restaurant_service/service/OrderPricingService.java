package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.dto.OrderItemRequest;
import com.tonyghouse.restaurant_service.dto.PriceBreakdown;
import com.tonyghouse.restaurant_service.entity.Order;

import java.util.List;

public interface OrderPricingService {

    PriceBreakdown calculate(List<OrderItemRequest> items);

    PriceBreakdown recalculateFromOrder(Order order);

}
