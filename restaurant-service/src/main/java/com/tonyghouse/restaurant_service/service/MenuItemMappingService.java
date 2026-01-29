package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.dto.MenuItemSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface MenuItemMappingService {

    void addItemToMenu(UUID menuId, UUID itemId);

    void removeItemFromMenu(UUID menuId, UUID itemId);

    List<MenuItemSummaryResponse> listItems(UUID menuId);
}
