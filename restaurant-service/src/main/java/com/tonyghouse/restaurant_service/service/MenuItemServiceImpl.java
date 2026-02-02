package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.dto.CreateMenuItemRequest;
import com.tonyghouse.restaurant_service.dto.MenuItemResponse;
import com.tonyghouse.restaurant_service.dto.UpdateMenuItemRequest;
import com.tonyghouse.restaurant_service.entity.MenuItem;
import com.tonyghouse.restaurant_service.exception.RestoRestaurantException;
import com.tonyghouse.restaurant_service.mapper.MenuItemMapper;
import com.tonyghouse.restaurant_service.repo.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository repository;
    private final Clock clock;


    @Override
    public MenuItemResponse createMenuItem(CreateMenuItemRequest request) {
        MenuItem menuItem = new MenuItem();
        menuItem.setId(UUID.randomUUID());
        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        menuItem.setPreparationTime(request.getPreparationTime());
        menuItem.setCategory(request.getCategory());
        menuItem.setFoodType(request.getFoodType());
        menuItem.setAvailable(true);
        menuItem.setCreatedAt(Instant.now(clock));

        return MenuItemMapper.toMenuItemResponse(repository.save(menuItem));
    }

    @Override
    public MenuItemResponse getMenuItem(UUID itemId) {
        return repository.findById(itemId)
                .map(MenuItemMapper::toMenuItemResponse)
                .orElseThrow(() ->
                        new IllegalArgumentException("Menu item not found"));
    }

    @Override
    public Page<MenuItemResponse> getMenuItems(Pageable pageable) {
        return repository.findAll(pageable)
                .map(MenuItemMapper::toMenuItemResponse);
    }


    @Override
    public MenuItemResponse updateMenuItem(UUID itemId, UpdateMenuItemRequest request) {
        MenuItem entity = repository.findById(itemId)
                .orElseThrow(() ->
                        new RestoRestaurantException("Menu item not found", HttpStatus.INTERNAL_SERVER_ERROR));

        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setPrice(request.getPrice());
        entity.setPreparationTime(request.getPreparationTime());
        entity.setCategory(request.getCategory());
        entity.setFoodType(request.getFoodType());

        return MenuItemMapper.toMenuItemResponse(repository.save(entity));
    }

    @Override
    public MenuItemResponse updateMenuItemAvailability(UUID itemId, boolean available) {
        MenuItem entity = repository.findById(itemId)
                .orElseThrow(() ->
                        new RestoRestaurantException("Menu item not found", HttpStatus.INTERNAL_SERVER_ERROR));

        entity.setAvailable(available);
        return MenuItemMapper.toMenuItemResponse(repository.save(entity));
    }


}
