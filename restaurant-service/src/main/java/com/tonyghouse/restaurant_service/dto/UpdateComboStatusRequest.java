package com.tonyghouse.restaurant_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateComboStatusRequest {

    @NotNull
    private Boolean active;
}
