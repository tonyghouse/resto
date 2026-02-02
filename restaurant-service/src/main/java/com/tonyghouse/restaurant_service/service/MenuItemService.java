package com.tonyghouse.restaurant_service.service;


import com.tonyghouse.restaurant_service.dto.CreateMenuItemRequest;
import com.tonyghouse.restaurant_service.dto.MenuItemResponse;
import com.tonyghouse.restaurant_service.dto.UpdateMenuItemRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MenuItemService {

    MenuItemResponse createMenuItem(CreateMenuItemRequest request);

    MenuItemResponse getMenuItem(UUID itemId);

    Page<MenuItemResponse> getMenuItems(Pageable pageable);

    MenuItemResponse updateMenuItem(UUID itemId, UpdateMenuItemRequest request);

    MenuItemResponse updateMenuItemAvailability(UUID itemId, boolean available);
}
