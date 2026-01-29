package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.constants.MenuType;
import com.tonyghouse.restaurant_service.dto.ComboSummaryResponse;
import com.tonyghouse.restaurant_service.dto.MenuWithItemsResponse;

import java.util.List;
import java.util.UUID;

public interface CustomerMenuService {

    MenuWithItemsResponse getActiveMenu(UUID branchId,  String timezone);

    MenuWithItemsResponse getMenuWithItems(UUID branchId, MenuType menuType);

    List<ComboSummaryResponse> getActiveCombos(UUID branchId);
}
