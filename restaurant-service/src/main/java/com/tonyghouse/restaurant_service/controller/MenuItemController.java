package com.tonyghouse.restaurant_service.controller;

import com.tonyghouse.restaurant_service.dto.CreateMenuItemRequest;
import com.tonyghouse.restaurant_service.dto.MenuItemResponse;
import com.tonyghouse.restaurant_service.dto.UpdateAvailabilityRequest;
import com.tonyghouse.restaurant_service.dto.UpdateMenuItemRequest;
import com.tonyghouse.restaurant_service.service.MenuItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/menu-items")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService service;


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public MenuItemResponse create(@Valid @RequestBody CreateMenuItemRequest request) {
        return service.create(request);
    }

    @GetMapping("/{itemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public MenuItemResponse get(@PathVariable UUID itemId) {
        return service.get(itemId);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<MenuItemResponse> list() {
        return service.getAll();
    }

    @PutMapping("/{itemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public MenuItemResponse update(
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateMenuItemRequest request) {
        return service.update(itemId, request);
    }

    @PatchMapping("/{itemId}/availability")
    @PreAuthorize("hasRole('ADMIN')")
    public MenuItemResponse updateAvailability(
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateAvailabilityRequest request) {
        return service.updateAvailability(itemId, request.getAvailable());
    }
}
