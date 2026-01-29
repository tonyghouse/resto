package com.tonyghouse.restaurant_service.controller;

import com.tonyghouse.restaurant_service.constants.MenuType;
import com.tonyghouse.restaurant_service.dto.CreateMenuRequest;
import com.tonyghouse.restaurant_service.dto.MenuResponse;
import com.tonyghouse.restaurant_service.dto.UpdateMenuStatusRequest;
import com.tonyghouse.restaurant_service.service.MenuService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/branches/{branchId}/menus")
    public MenuResponse createMenu(@PathVariable UUID branchId, @Valid @RequestBody CreateMenuRequest request) {
        return menuService.createMenu(branchId, request);
    }


    @GetMapping("/branches/{branchId}/menus/{menuType}")
    @PreAuthorize("hasRole('ADMIN')")
    public MenuResponse getMenuByType(@PathVariable UUID branchId, @PathVariable MenuType menuType) {
        return menuService.getMenuByType(branchId, menuType);
    }

    @GetMapping("/branches/{branchId}/menus")
    @PreAuthorize("hasRole('ADMIN')")
    public List<MenuResponse> listMenus(@PathVariable UUID branchId) {
        return menuService.getMenusByBranch(branchId);
    }

    @PatchMapping("/menus/{menuId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public MenuResponse updateMenuStatus(@PathVariable UUID menuId, @Valid @RequestBody UpdateMenuStatusRequest request) {
        return menuService.updateMenuStatus(menuId, request.getActive());
    }
}
