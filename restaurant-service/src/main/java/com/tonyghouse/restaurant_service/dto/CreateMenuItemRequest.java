package com.tonyghouse.restaurant_service.dto;

import com.tonyghouse.restaurant_service.constants.FoodType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateMenuItemRequest {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    @Positive
    private BigDecimal price;

    @NotNull
    @Positive
    private Integer preparationTime;

    @NotBlank
    private String category;

    @NotNull
    private FoodType foodType;
}
