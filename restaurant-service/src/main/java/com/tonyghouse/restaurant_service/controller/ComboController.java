package com.tonyghouse.restaurant_service.controller;

import com.tonyghouse.restaurant_service.dto.ComboResponse;
import com.tonyghouse.restaurant_service.dto.CreateComboRequest;
import com.tonyghouse.restaurant_service.dto.UpdateComboRequest;
import com.tonyghouse.restaurant_service.dto.UpdateComboStatusRequest;
import com.tonyghouse.restaurant_service.service.ComboService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/combos")
@RequiredArgsConstructor
public class ComboController {

    private final ComboService service;


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ComboResponse create(@Valid @RequestBody CreateComboRequest request) {
        return service.create(request);
    }

    @GetMapping("/{comboId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ComboResponse get(@PathVariable UUID comboId) {
        return service.get(comboId);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ComboResponse> list() {
        return service.getAll();
    }

    @PutMapping("/{comboId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ComboResponse update(
            @PathVariable UUID comboId,
            @Valid @RequestBody UpdateComboRequest request) {
        return service.update(comboId, request);
    }

    @PatchMapping("/{comboId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ComboResponse updateStatus(
            @PathVariable UUID comboId,
            @Valid @RequestBody UpdateComboStatusRequest request) {
        return service.updateStatus(comboId, request.getActive());
    }

    @PostMapping("/{comboId}/items")
    @PreAuthorize("hasRole('ADMIN')")
    public void addItem(
            @PathVariable UUID comboId,
            @RequestParam UUID itemId) {
        service.addItem(comboId, itemId);
    }

    @DeleteMapping("/{comboId}/items/{itemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public void removeItem(
            @PathVariable UUID comboId,
            @PathVariable UUID itemId) {
        service.removeItem(comboId, itemId);
    }
}
