package com.tonyghouse.restaurant_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class RestoRestaurantError {
    private String message;
    private String code;
    private Instant timestamp;
}
