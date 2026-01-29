package com.tonyghouse.restaurant_service.service;


import com.tonyghouse.restaurant_service.constants.MenuType;
import com.tonyghouse.restaurant_service.dto.CreateMenuRequest;
import com.tonyghouse.restaurant_service.dto.MenuResponse;

import java.util.List;
import java.util.UUID;

public interface MenuService {

    MenuResponse createMenu(UUID branchId, CreateMenuRequest request);

    MenuResponse getMenuByType(UUID branchId, MenuType menuType);

    List<MenuResponse> getMenusByBranch(UUID branchId);

    MenuResponse updateMenuStatus(UUID menuId, boolean active);
}
