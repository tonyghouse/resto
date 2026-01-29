package com.tonyghouse.restaurant_service.service;


import com.tonyghouse.restaurant_service.dto.CreateMenuItemRequest;
import com.tonyghouse.restaurant_service.dto.MenuItemResponse;
import com.tonyghouse.restaurant_service.dto.UpdateMenuItemRequest;

import java.util.List;
import java.util.UUID;

public interface MenuItemService {

    MenuItemResponse create(CreateMenuItemRequest request);

    MenuItemResponse get(UUID itemId);

    List<MenuItemResponse> getAll();

    MenuItemResponse update(UUID itemId, UpdateMenuItemRequest request);

    MenuItemResponse updateAvailability(UUID itemId, boolean available);
}
