package com.tonyghouse.restaurant_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateAvailabilityRequest {

    @NotNull
    private Boolean available;
}
