package com.tonyghouse.restaurant_service.controller;

import com.tonyghouse.restaurant_service.constants.MenuType;
import com.tonyghouse.restaurant_service.dto.ComboSummaryResponse;
import com.tonyghouse.restaurant_service.dto.MenuWithItemsResponse;
import com.tonyghouse.restaurant_service.service.CustomerMenuService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/branches")
public class CustomerMenuController {

    private final CustomerMenuService service;

    public CustomerMenuController(CustomerMenuService service) {
        this.service = service;
    }

    @GetMapping("/{branchId}/menus/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    public MenuWithItemsResponse getActiveMenu(
            @RequestHeader(value = "X-Timezone", required = false) String timezone,
            @PathVariable UUID branchId) {
        return service.getActiveMenu(branchId, timezone);
    }

    @GetMapping("/{branchId}/menus/{menuType}/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    public MenuWithItemsResponse getMenuWithItems(
            @PathVariable UUID branchId,
            @PathVariable MenuType menuType) {
        return service.getMenuWithItems(branchId, menuType);
    }

    @GetMapping("/{branchId}/combos")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    public List<ComboSummaryResponse> getCombos(
            @PathVariable UUID branchId) {
        return service.getActiveCombos(branchId);
    }
}
