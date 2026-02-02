package com.tonyghouse.restaurant_service.controller;

import com.tonyghouse.restaurant_service.dto.ComboResponse;
import com.tonyghouse.restaurant_service.dto.CreateComboRequest;
import com.tonyghouse.restaurant_service.dto.UpdateComboRequest;
import com.tonyghouse.restaurant_service.dto.UpdateComboStatusRequest;
import com.tonyghouse.restaurant_service.service.ComboService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/combos")
@RequiredArgsConstructor
public class ComboController {

    private final ComboService service;


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ComboResponse create(@Valid @RequestBody CreateComboRequest request) {

        log.info("Create combo request received. name={}", request.getName());
        log.debug("CreateComboRequest payload={}", request);

        ComboResponse response = service.create(request);

        log.info("Combo created successfully. comboId={}, name={}",
                response.getId(), response.getName());

        return response;
    }


    @GetMapping("/{comboId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ComboResponse get(@PathVariable UUID comboId) {

        log.debug("Fetching combo. comboId={}", comboId);

        return service.get(comboId);
    }


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<ComboResponse> list(Pageable pageable) {
        log.debug("Listing combos with paging: {}", pageable);
        Page<ComboResponse> combos = service.getAll(pageable);
        log.info("Combos fetched successfully. count={}", combos.getTotalElements());

        return combos;
    }



    @PutMapping("/{comboId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ComboResponse update(
            @PathVariable UUID comboId,
            @Valid @RequestBody UpdateComboRequest request) {

        log.info("Updating combo. comboId={}", comboId);
        log.debug("UpdateComboRequest payload={}", request);

        ComboResponse response = service.update(comboId, request);

        log.info("Combo updated successfully. comboId={}", comboId);

        return response;
    }


    @PatchMapping("/{comboId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ComboResponse updateStatus(
            @PathVariable UUID comboId,
            @Valid @RequestBody UpdateComboStatusRequest request) {

        log.info("Updating combo status. comboId={}, active={}",
                comboId, request.getActive());

        ComboResponse response = service.updateStatus(comboId, request.getActive());

        log.info("Combo status updated successfully. comboId={}, active={}",
                comboId, request.getActive());

        return response;
    }


    @PostMapping("/{comboId}/items")
    @PreAuthorize("hasRole('ADMIN')")
    public void addItem(
            @PathVariable UUID comboId,
            @RequestParam UUID itemId) {

        log.info("Adding item to combo. comboId={}, itemId={}", comboId, itemId);
        service.addItem(comboId, itemId);
        log.info("Item added successfully. comboId={}, itemId={}", comboId, itemId);
    }


    @DeleteMapping("/{comboId}/items/{itemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public void removeItem(
            @PathVariable UUID comboId,
            @PathVariable UUID itemId) {
        log.warn("Removing item from combo. comboId={}, itemId={}", comboId, itemId);
        service.removeItem(comboId, itemId);

        log.warn("Item removed successfully. comboId={}, itemId={}", comboId, itemId);
    }
}
