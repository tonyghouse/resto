package com.tonyghouse.restaurant_service.controller;

import com.tonyghouse.restaurant_service.constants.MenuType;
import com.tonyghouse.restaurant_service.dto.CreateMenuRequest;
import com.tonyghouse.restaurant_service.dto.MenuResponse;
import com.tonyghouse.restaurant_service.dto.UpdateMenuStatusRequest;
import com.tonyghouse.restaurant_service.service.MenuService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/branches/{branchId}/menus")
    public MenuResponse createMenu(
            @PathVariable UUID branchId,
            @Valid @RequestBody CreateMenuRequest request) {

        log.info("Create menu request received. branchId={}, type={}",
                branchId, request.getMenuType());
        log.debug("CreateMenuRequest payload={}", request);

        MenuResponse response = menuService.createMenu(branchId, request);

        log.info("Menu created successfully. menuId={}, branchId={}, type={}",
                response.getId(), branchId, response.getMenuType());

        return response;
    }


    @GetMapping("/branches/{branchId}/menus/{menuType}")
    @PreAuthorize("hasRole('ADMIN')")
    public MenuResponse getMenuByType(
            @PathVariable UUID branchId,
            @PathVariable MenuType menuType) {

        log.debug("Fetching menu by type. branchId={}, menuType={}",
                branchId, menuType);

        return menuService.getMenuByType(branchId, menuType);
    }


    @GetMapping("/branches/{branchId}/menus")
    @PreAuthorize("hasRole('ADMIN')")
    public List<MenuResponse> listMenus(@PathVariable UUID branchId) {

        log.debug("Listing menus for branch. branchId={}", branchId);

        List<MenuResponse> menus = menuService.getMenusByBranch(branchId);

        log.info("Menus fetched successfully. branchId={}, count={}",
                branchId, menus.size());

        return menus;
    }


    @PatchMapping("/menus/{menuId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public MenuResponse updateMenuStatus(
            @PathVariable UUID menuId,
            @Valid @RequestBody UpdateMenuStatusRequest request) {

        log.info("Updating menu status. menuId={}, active={}",
                menuId, request.getActive());

        MenuResponse response =
                menuService.updateMenuStatus(menuId, request.getActive());

        log.info("Menu status updated successfully. menuId={}, active={}",
                menuId, request.getActive());

        return response;
    }
}
