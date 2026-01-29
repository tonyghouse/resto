package com.tonyghouse.restaurant_service.dto;

import com.tonyghouse.restaurant_service.constants.MenuType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class CreateMenuRequest {

    @NotNull
    private MenuType menuType;

    @NotNull
    private LocalTime validFrom;

    @NotNull
    private LocalTime validTo;

}
