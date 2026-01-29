package com.tonyghouse.restaurant_service.mapper;

import com.tonyghouse.restaurant_service.dto.ComboResponse;
import com.tonyghouse.restaurant_service.entity.Combo;
import com.tonyghouse.restaurant_service.entity.MenuItem;

public class ComboMapper {


    public static ComboResponse toResponse(Combo combo) {
        ComboResponse r = new ComboResponse();
        r.setId(combo.getId());
        r.setName(combo.getName());
        r.setDescription(combo.getDescription());
        r.setComboPrice(combo.getComboPrice());
        r.setActive(combo.isActive());
        r.setCreatedAt(combo.getCreatedAt());
        r.setItemIds(
                combo.getItems().stream()
                        .map(MenuItem::getId)
                        .toList()
        );
        return r;
    }
}
