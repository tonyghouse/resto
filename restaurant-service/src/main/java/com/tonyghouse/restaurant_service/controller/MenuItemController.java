package com.tonyghouse.restaurant_service.controller;

import com.tonyghouse.restaurant_service.dto.CreateMenuItemRequest;
import com.tonyghouse.restaurant_service.dto.MenuItemResponse;
import com.tonyghouse.restaurant_service.dto.UpdateAvailabilityRequest;
import com.tonyghouse.restaurant_service.dto.UpdateMenuItemRequest;
import com.tonyghouse.restaurant_service.service.MenuItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/menu-items")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService service;


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public MenuItemResponse createMenuItem(@Valid @RequestBody CreateMenuItemRequest request) {

        log.info("Create menu item request received. name={}, price={}",
                request.getName(), request.getPrice());
        log.debug("CreateMenuItemRequest payload={}", request);

        MenuItemResponse response = service.createMenuItem(request);

        log.info("Menu item created successfully. itemId={}, name={}",
                response.getId(), response.getName());

        return response;
    }


    @GetMapping("/{itemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public MenuItemResponse getMenuItem(@PathVariable UUID itemId) {

        log.debug("Fetching menu item. itemId={}", itemId);

        return service.getMenuItem(itemId);
    }


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<MenuItemResponse> getMenuItems(Pageable pageable) {

        log.debug("Listing menu items with paging: {}", pageable);

        Page<MenuItemResponse> items = service.getMenuItems(pageable);

        log.info("Menu items fetched. count={}", items.getTotalElements());

        return items;
    }



    @PutMapping("/{itemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public MenuItemResponse updateMenuItem(
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateMenuItemRequest request) {

        log.info("Updating menu item. itemId={}", itemId);
        log.debug("UpdateMenuItemRequest payload={}", request);

        MenuItemResponse response = service.updateMenuItem(itemId, request);

        log.info("Menu item updated successfully. itemId={}", itemId);

        return response;
    }


    @PatchMapping("/{itemId}/availability")
    @PreAuthorize("hasRole('ADMIN')")
    public MenuItemResponse updateMenuItemAvailability(
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateAvailabilityRequest request) {

        log.info("Updating item availability. itemId={}, available={}",
                itemId, request.getAvailable());

        MenuItemResponse response =
                service.updateMenuItemAvailability(itemId, request.getAvailable());

        log.info("Item availability updated successfully. itemId={}, available={}",
                itemId, request.getAvailable());

        return response;
    }
}
