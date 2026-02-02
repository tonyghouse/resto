package com.tonyghouse.restaurant_service.controller;

import com.tonyghouse.restaurant_service.constants.MenuType;
import com.tonyghouse.restaurant_service.dto.ComboSummaryResponse;
import com.tonyghouse.restaurant_service.dto.MenuWithItemsResponse;
import com.tonyghouse.restaurant_service.service.CustomerMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
public class CustomerMenuController {

    private final CustomerMenuService service;


    @GetMapping("/{branchId}/menus/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    public MenuWithItemsResponse getActiveMenu(
            @RequestHeader(value = "X-Timezone", required = false) String timezone,
            @PathVariable UUID branchId) {
        log.debug("Fetching active menu. branchId={}, timezone={}", branchId, timezone);
        MenuWithItemsResponse response = service.getActiveMenu(branchId, timezone);
        log.debug("Active menu fetched successfully. branchId={}, itemCount={}",
                branchId,
                response.getItems() != null ? response.getItems().size() : 0);
        return response;
    }


    @GetMapping("/{branchId}/menus/{menuType}/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    public MenuWithItemsResponse getMenuWithItems(
            @PathVariable UUID branchId,
            @PathVariable MenuType menuType) {
        log.debug("Fetching menu items. branchId={}, menuType={}", branchId, menuType);
        MenuWithItemsResponse response = service.getMenuWithItems(branchId, menuType);
        log.debug("Menu items fetched. branchId={}, menuType={}, itemCount={}",
                branchId,
                menuType,
                response.getItems() != null ? response.getItems().size() : 0);
        return response;
    }


    @GetMapping("/{branchId}/combos")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    public List<ComboSummaryResponse> getCombos(@PathVariable UUID branchId) {
        log.debug("Fetching active combos. branchId={}", branchId);
        List<ComboSummaryResponse> combos = service.getActiveCombos(branchId);
        log.info("Active combos fetched. branchId={}, count={}", branchId, combos.size());
        return combos;
    }
}
