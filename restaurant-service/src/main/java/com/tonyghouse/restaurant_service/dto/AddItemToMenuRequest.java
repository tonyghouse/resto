package com.tonyghouse.restaurant_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AddItemToMenuRequest {

    @NotNull
    private UUID itemId;

}
