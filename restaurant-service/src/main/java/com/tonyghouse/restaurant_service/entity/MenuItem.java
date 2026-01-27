package com.tonyghouse.restaurant_service.entity;

import com.tonyghouse.restaurant_service.constants.FoodType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "menu_item")
@Getter
@Setter
public class MenuItem extends BaseEntity {

    private String name;
    private String description;
    private BigDecimal price;
    private int preparationTime;
    private String category;

    @Enumerated(EnumType.STRING)
    private FoodType foodType;

    private boolean available;
}
