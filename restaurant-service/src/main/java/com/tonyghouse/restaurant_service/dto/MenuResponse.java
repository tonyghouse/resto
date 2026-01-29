package com.tonyghouse.restaurant_service.dto;

import com.tonyghouse.restaurant_service.constants.MenuType;
import lombok.Data;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class MenuResponse {

    private UUID id;
    private UUID branchId;
    private MenuType menuType;
    private LocalTime validFrom;
    private LocalTime validTo;
    private boolean active;
    private Instant createdAt;
}
