package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.dto.CreateMenuItemRequest;
import com.tonyghouse.restaurant_service.dto.MenuItemResponse;
import com.tonyghouse.restaurant_service.dto.UpdateMenuItemRequest;
import com.tonyghouse.restaurant_service.entity.MenuItem;
import com.tonyghouse.restaurant_service.exception.RestoRestaurantException;
import com.tonyghouse.restaurant_service.mapper.MenuItemMapper;
import com.tonyghouse.restaurant_service.repo.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

        return MenuItemMapper.toMenuItemResponse(repository.save(menuItem));
    }

    @Override
    public MenuItemResponse get(UUID itemId) {
        return repository.findById(itemId)
                .map(MenuItemMapper::toMenuItemResponse)
                .orElseThrow(() ->
                        new IllegalArgumentException("Menu item not found"));
    }

    @Override
    public List<MenuItemResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(MenuItemMapper::toMenuItemResponse)
                .toList();
    }

    @Override
    public MenuItemResponse update(UUID itemId, UpdateMenuItemRequest request) {
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
    public MenuItemResponse updateAvailability(UUID itemId, boolean available) {
        MenuItem entity = repository.findById(itemId)
                .orElseThrow(() ->
                        new RestoRestaurantException("Menu item not found", HttpStatus.INTERNAL_SERVER_ERROR));

        entity.setAvailable(available);
        return MenuItemMapper.toMenuItemResponse(repository.save(entity));
    }


}
