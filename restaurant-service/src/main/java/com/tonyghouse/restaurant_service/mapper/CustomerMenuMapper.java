package com.tonyghouse.restaurant_service.mapper;

import com.tonyghouse.restaurant_service.dto.ComboSummaryResponse;
import com.tonyghouse.restaurant_service.dto.MenuItemResponse;
import com.tonyghouse.restaurant_service.dto.MenuWithItemsResponse;
import com.tonyghouse.restaurant_service.entity.Combo;
import com.tonyghouse.restaurant_service.entity.Menu;
import com.tonyghouse.restaurant_service.entity.MenuItem;

import java.util.Set;

public class CustomerMenuMapper {

    public static MenuWithItemsResponse toMenuResponse(Menu menu, Set<MenuItem> menuItems) {
        MenuWithItemsResponse r = new MenuWithItemsResponse();
        r.setMenuId(menu.getId());
        r.setMenuType(menu.getMenuType());
        r.setValidFrom(menu.getValidFrom());
        r.setValidTo(menu.getValidTo());

        r.setItems(
                menuItems
                        .stream()
                        .filter(MenuItem::getAvailable)
                        .map(CustomerMenuMapper::toItemResponse)
                        .toList()
        );
        return r;
    }

    public static MenuItemResponse toItemResponse(MenuItem item) {
        MenuItemResponse r = new MenuItemResponse();
        r.setId(item.getId());
        r.setName(item.getName());
        r.setDescription(item.getDescription());
        r.setPrice(item.getPrice());
        r.setFoodType(item.getFoodType());
        r.setCategory(item.getCategory());
        r.setAvailable(item.getAvailable());
        return r;
    }

    public static ComboSummaryResponse toComboResponse(Combo combo,  Set<MenuItem> items) {
        ComboSummaryResponse r = new ComboSummaryResponse();
        r.setId(combo.getId());
        r.setName(combo.getName());
        r.setDescription(combo.getDescription());
        r.setComboPrice(combo.getComboPrice());
        r.setItemIds(
                items
                        .stream()
                        .map(MenuItem::getId)
                        .toList()
        );
        return r;
    }
}
