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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MenuItemMappingServiceImpl implements MenuItemMappingService {

    private final MenuRepository menuRepository;
    private final MenuItemRepository menuItemRepository;


    @Override
    public void addItemToMenu(UUID menuId, UUID itemId) {
        log.info("Adding item to menu. menuId={} itemId={}", menuId, itemId);

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new RestoRestaurantException("Menu not found", HttpStatus.NOT_FOUND));

        log.debug("Menu found. menuId={}", menuId);

        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new RestoRestaurantException("Menu item not found", HttpStatus.NOT_FOUND));
        log.debug("Menu item found. itemId={}", itemId);

        // idempotent
        if (menu.getItems().contains(item)) {
            log.info("Item already exists in menu. Skipping add. menuId={} itemId={}", menuId, itemId);

            return;
        }
        menu.getItems().add(item);
        menuRepository.save(menu);
        log.info("Item added to menu successfully. menuId={} itemId={}", menuId, itemId);
    }

    @Override
    public void removeItemFromMenu(UUID menuId, UUID itemId) {
        log.info("Removing item from menu. menuId={} itemId={}", menuId, itemId);

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new RestoRestaurantException("Menu not found",HttpStatus.NOT_FOUND));

        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new RestoRestaurantException("Menu item not found", HttpStatus.NOT_FOUND));
        log.debug("Menu and item loaded. menuId={} itemId={}", menuId, itemId);

        menu.getItems().remove(item);
        menuRepository.save(menu);
        log.info("Item removed from menu successfully. menuId={} itemId={}", menuId, itemId);

    }

    @Override
    public List<MenuItemSummaryResponse> listItems(UUID menuId) {
        log.debug("Listing items for menuId={}", menuId);

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new RestoRestaurantException("Menu not found", HttpStatus.NOT_FOUND));
        log.debug("Menu found. menuId={} itemsCount={}", menuId, menu.getItems().size());

        return menu.getItems()
                .stream()
                .map(MenuMapper::toMenuItemSummaryResponse)
                .toList();
    }


}
