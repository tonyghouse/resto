package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.dto.MenuItemSummaryResponse;
import com.tonyghouse.restaurant_service.entity.Menu;
import com.tonyghouse.restaurant_service.entity.MenuItem;
import com.tonyghouse.restaurant_service.exception.RestoRestaurantException;
import com.tonyghouse.restaurant_service.mapper.MenuMapper;
import com.tonyghouse.restaurant_service.repo.MenuItemRepository;
import com.tonyghouse.restaurant_service.repo.MenuRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class MenuItemMappingServiceImpl implements MenuItemMappingService {

    private final MenuRepository menuRepository;
    private final MenuItemRepository menuItemRepository;


    @Override
    public void addItemToMenu(UUID menuId, UUID itemId) {

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new RestoRestaurantException("Menu not found", HttpStatus.NOT_FOUND));

        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new RestoRestaurantException("Menu item not found", HttpStatus.NOT_FOUND));

        // idempotent
        if (menu.getItems().contains(item)) {
            return;
        }

        menu.getItems().add(item);
        menuRepository.save(menu);
    }

    @Override
    public void removeItemFromMenu(UUID menuId, UUID itemId) {

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new RestoRestaurantException("Menu not found",HttpStatus.NOT_FOUND));

        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new RestoRestaurantException("Menu item not found", HttpStatus.NOT_FOUND));

        menu.getItems().remove(item);
        menuRepository.save(menu);
    }

    @Override
    public List<MenuItemSummaryResponse> listItems(UUID menuId) {

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new RestoRestaurantException("Menu not found", HttpStatus.NOT_FOUND));

        return menu.getItems()
                .stream()
                .map(MenuMapper::toMenuItemSummaryResponse)
                .toList();
    }


}
