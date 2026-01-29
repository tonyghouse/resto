package com.tonyghouse.restaurant_service.dto;

import com.tonyghouse.restaurant_service.constants.MenuType;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
public class MenuWithItemsResponse {

    private UUID menuId;
    private MenuType menuType;
    private LocalTime validFrom;
    private LocalTime validTo;
    private List<MenuItemResponse> items;
}
