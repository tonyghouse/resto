package com.tonyghouse.restaurant_service.mapper;

import com.tonyghouse.restaurant_service.dto.MenuItemResponse;
import com.tonyghouse.restaurant_service.entity.MenuItem;

public class MenuItemMapper {

    public static MenuItemResponse toMenuItemResponse(MenuItem e) {
        MenuItemResponse r = new MenuItemResponse();
        r.setId(e.getId());
        r.setName(e.getName());
        r.setDescription(e.getDescription());
        r.setPrice(e.getPrice());
        r.setPreparationTime(e.getPreparationTime());
        r.setCategory(e.getCategory());
        r.setFoodType(e.getFoodType());
        r.setAvailable(e.getAvailable());
        r.setCreatedAt(e.getCreatedAt());
        return r;
    }
}
