package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.dto.CreateMenuItemRequest;
import com.tonyghouse.restaurant_service.dto.MenuItemResponse;
import com.tonyghouse.restaurant_service.dto.UpdateMenuItemRequest;
import com.tonyghouse.restaurant_service.entity.MenuItem;
import com.tonyghouse.restaurant_service.exception.RestoRestaurantException;
import com.tonyghouse.restaurant_service.mapper.MenuItemMapper;
import com.tonyghouse.restaurant_service.repo.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository repository;
    private final Clock clock;


    @Override
    public MenuItemResponse createMenuItem(CreateMenuItemRequest request) {
        log.info("Creating menu item. name={} price={} category={}",
                request.getName(), request.getPrice(), request.getCategory());

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

        MenuItem saved = repository.save(menuItem);
        log.info("Menu item created successfully. itemId={}", saved.getId());
        return MenuItemMapper.toMenuItemResponse(saved);
    }

    @Override
    public MenuItemResponse getMenuItem(UUID itemId) {
        log.debug("Fetching menu item. itemId={}", itemId);
        return repository.findById(itemId)
                .map(MenuItemMapper::toMenuItemResponse)
                .orElseThrow(() ->
                        new IllegalArgumentException("Menu item not found"));
    }

    @Override
    public Page<MenuItemResponse> getMenuItems(Pageable pageable) {
        log.debug("Fetching paginated menu items. page={} size={}",
                pageable.getPageNumber(), pageable.getPageSize());
        Page<MenuItemResponse> pageResult = repository.findAll(pageable)
                .map(MenuItemMapper::toMenuItemResponse);
        log.info("Returning {} menu items", pageResult.getNumberOfElements());
        return pageResult;

    }


    @Override
    public MenuItemResponse updateMenuItem(UUID itemId, UpdateMenuItemRequest request) {
        log.info("Updating menu item. itemId={}", itemId);
        MenuItem entity = repository.findById(itemId)
                .orElseThrow(() ->
                        new RestoRestaurantException("Menu item not found", HttpStatus.INTERNAL_SERVER_ERROR));

        log.debug("Menu item loaded for update. itemId={}", itemId);

        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setPrice(request.getPrice());
        entity.setPreparationTime(request.getPreparationTime());
        entity.setCategory(request.getCategory());
        entity.setFoodType(request.getFoodType());

        MenuItem saved = repository.save(entity);
        log.info("Menu item updated successfully. itemId={}", itemId);
        return MenuItemMapper.toMenuItemResponse(saved);

    }

    @Override
    public MenuItemResponse updateMenuItemAvailability(UUID itemId, boolean available) {
        log.info("Updating availability. itemId={} available={}", itemId, available);

        MenuItem entity = repository.findById(itemId)
                .orElseThrow(() ->
                        new RestoRestaurantException("Menu item not found", HttpStatus.INTERNAL_SERVER_ERROR));

        entity.setAvailable(available);
        MenuItem saved = repository.save(entity);
        log.info("Availability updated. itemId={} available={}", itemId, available);
        return MenuItemMapper.toMenuItemResponse(saved);

    }


}
