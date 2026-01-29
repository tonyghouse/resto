package com.tonyghouse.restaurant_service.controller;

import com.tonyghouse.restaurant_service.dto.AddItemToMenuRequest;
import com.tonyghouse.restaurant_service.dto.MenuItemSummaryResponse;
import com.tonyghouse.restaurant_service.service.MenuItemMappingService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/menus")
public class MenuItemMappingController {

    private final MenuItemMappingService service;

    public MenuItemMappingController(MenuItemMappingService service) {
        this.service = service;
    }

    @PostMapping("/{menuId}/items")
    @PreAuthorize("hasRole('ADMIN')")
    public void addItem(
            @PathVariable UUID menuId,
            @Valid @RequestBody AddItemToMenuRequest request) {
        service.addItemToMenu(menuId, request.getItemId());
    }

    @DeleteMapping("/{menuId}/items/{itemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public void removeItem(
            @PathVariable UUID menuId,
            @PathVariable UUID itemId) {
        service.removeItemFromMenu(menuId, itemId);
    }

    @GetMapping("/{menuId}/items")
    @PreAuthorize("hasRole('ADMIN')")
    public List<MenuItemSummaryResponse> listItems(
            @PathVariable UUID menuId) {
        return service.listItems(menuId);
    }
}
