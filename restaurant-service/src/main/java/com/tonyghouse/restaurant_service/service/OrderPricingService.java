package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.dto.OrderItemRequest;
import com.tonyghouse.restaurant_service.dto.PriceBreakdown;

import java.util.List;

public interface OrderPricingService {

    PriceBreakdown calculate(List<OrderItemRequest> items);
}
