package com.tonyghouse.restaurant_service.dto;

import com.tonyghouse.restaurant_service.constants.FoodType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class MenuItemSummaryResponse {

    private UUID id;
    private String name;
    private BigDecimal price;
    private boolean available;
    private FoodType foodType;
}
