package com.tonyghouse.restaurant_service.dto;

import com.tonyghouse.restaurant_service.constants.FoodType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class MenuItemResponse {

    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer preparationTime;
    private String category;
    private FoodType foodType;
    private Boolean available;
    private Instant createdAt;
}
