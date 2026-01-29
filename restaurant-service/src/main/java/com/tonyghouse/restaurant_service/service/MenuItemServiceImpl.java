package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.dto.CreateMenuItemRequest;
import com.tonyghouse.restaurant_service.dto.MenuItemResponse;
import com.tonyghouse.restaurant_service.dto.UpdateMenuItemRequest;
import com.tonyghouse.restaurant_service.entity.MenuItem;
import com.tonyghouse.restaurant_service.repo.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository repository;


    @Override
    public MenuItemResponse create(CreateMenuItemRequest request) {
        MenuItem menuItem = new MenuItem();
        menuItem.setId(UUID.randomUUID());
        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        menuItem.setPreparationTime(request.getPreparationTime());
        menuItem.setCategory(request.getCategory());
        menuItem.setFoodType(request.getFoodType());
        menuItem.setAvailable(true);
        menuItem.setCreatedAt(Instant.now());

        return toResponse(repository.save(menuItem));
    }

    @Override
    public MenuItemResponse get(UUID itemId) {
        return repository.findById(itemId)
                .map(this::toResponse)
                .orElseThrow(() ->
                        new IllegalArgumentException("Menu item not found"));
    }

    @Override
    public List<MenuItemResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public MenuItemResponse update(UUID itemId, UpdateMenuItemRequest request) {
        MenuItem entity = repository.findById(itemId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Menu item not found"));

        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setPrice(request.getPrice());
        entity.setPreparationTime(request.getPreparationTime());
        entity.setCategory(request.getCategory());
        entity.setFoodType(request.getFoodType());

        return toResponse(repository.save(entity));
    }

    @Override
    public MenuItemResponse updateAvailability(UUID itemId, boolean available) {
        MenuItem entity = repository.findById(itemId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Menu item not found"));

        entity.setAvailable(available);
        return toResponse(repository.save(entity));
    }

    private MenuItemResponse toResponse(MenuItem e) {
        MenuItemResponse r = new MenuItemResponse();
        r.setId(e.getId());
        r.setName(e.getName());
        r.setDescription(e.getDescription());
        r.setPrice(e.getPrice());
        r.setPreparationTime(e.getPreparationTime());
        r.setCategory(e.getCategory());
        r.setFoodType(e.getFoodType());
        r.setAvailable(e.getAvailable());
        r.setCreatedAt(e.getCreatedAt());
        return r;
    }
}
