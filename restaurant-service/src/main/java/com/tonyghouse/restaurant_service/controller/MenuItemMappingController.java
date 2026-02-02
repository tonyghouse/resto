package com.tonyghouse.restaurant_service.controller;

import com.tonyghouse.restaurant_service.dto.AddItemToMenuRequest;
import com.tonyghouse.restaurant_service.dto.MenuItemSummaryResponse;
import com.tonyghouse.restaurant_service.service.MenuItemMappingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuItemMappingController {

    private final MenuItemMappingService service;


    @PostMapping("/{menuId}/items")
    @PreAuthorize("hasRole('ADMIN')")
    public void addItem(
            @PathVariable UUID menuId,
            @Valid @RequestBody AddItemToMenuRequest request) {
        log.info("Adding item to menu. menuId={}, itemId={}",
                menuId, request.getItemId());
        log.debug("AddItemToMenuRequest payload={}", request);
        service.addItemToMenu(menuId, request.getItemId());
        log.info("Item added successfully. menuId={}, itemId={}",
                menuId, request.getItemId());
    }


    @DeleteMapping("/{menuId}/items/{itemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public void removeItem(
            @PathVariable UUID menuId,
            @PathVariable UUID itemId) {
        log.warn("Removing item from menu. menuId={}, itemId={}",
                menuId, itemId);
        service.removeItemFromMenu(menuId, itemId);
        log.warn("Item removed successfully. menuId={}, itemId={}",
                menuId, itemId);
    }


    @GetMapping("/{menuId}/items")
    @PreAuthorize("hasRole('ADMIN')")
    public List<MenuItemSummaryResponse> listItems(
            @PathVariable UUID menuId) {
        log.debug("Listing menu items. menuId={}", menuId);
        List<MenuItemSummaryResponse> items = service.listItems(menuId);
        log.info("Menu items fetched. menuId={}, count={}", menuId, items.size());
        return items;
    }
}
