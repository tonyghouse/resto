package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.constants.FoodType;
import com.tonyghouse.restaurant_service.constants.MenuType;
import com.tonyghouse.restaurant_service.dto.MenuItemSummaryResponse;
import com.tonyghouse.restaurant_service.entity.Branch;
import com.tonyghouse.restaurant_service.entity.Menu;
import com.tonyghouse.restaurant_service.entity.MenuItem;
import com.tonyghouse.restaurant_service.exception.RestoRestaurantException;
import com.tonyghouse.restaurant_service.repo.MenuItemRepository;
import com.tonyghouse.restaurant_service.repo.MenuRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MenuItemMappingServiceImplTest {

    @Mock
    MenuRepository menuRepository;

    @Mock
    MenuItemRepository menuItemRepository;

    @InjectMocks
    MenuItemMappingServiceImpl service;

    @Test
    void addItemToMenu_success() {
        UUID menuId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        Menu menu = new Menu();
        MenuItem item = new MenuItem();

        Mockito.when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));
        Mockito.when(menuItemRepository.findById(itemId)).thenReturn(Optional.of(item));

        service.addItemToMenu(menuId, itemId);

        assertTrue(menu.getItems().contains(item));
        Mockito.verify(menuRepository).save(menu);
    }

    @Test
    void addItemToMenu_idempotent() {
        UUID menuId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        Menu menu = new Menu();
        MenuItem item = new MenuItem();
        menu.getItems().add(item);

        Mockito.when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));
        Mockito.when(menuItemRepository.findById(itemId)).thenReturn(Optional.of(item));

        service.addItemToMenu(menuId, itemId);

        Mockito.verify(menuRepository, Mockito.never()).save(menu);
    }

    @Test
    void removeItemFromMenu_success() {
        UUID menuId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        Menu menu = new Menu();
        MenuItem item = new MenuItem();
        menu.getItems().add(item);

        Mockito.when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));
        Mockito.when(menuItemRepository.findById(itemId)).thenReturn(Optional.of(item));

        service.removeItemFromMenu(menuId, itemId);

        assertFalse(menu.getItems().contains(item));
        Mockito.verify(menuRepository).save(menu);
    }

    @Test
    void addItemToMenu_menuNotFound() {
        Mockito.when(menuRepository.findById(Mockito.any()))
                .thenReturn(Optional.empty());

        assertThrows(RestoRestaurantException.class,
                () -> service.addItemToMenu(UUID.randomUUID(), UUID.randomUUID()));
    }


    @Test
    void listItems_success() {
        UUID menuId = UUID.randomUUID();

        Menu menu = new Menu();
        menu.setId(UUID.randomUUID());
        menu.setBranch(new Branch());
        menu.setMenuType(MenuType.BREAKFAST);
        menu.setValidFrom(LocalTime.now());
        menu.setValidTo(LocalTime.now());
        menu.setActive(true);
        menu.setItems(new HashSet<>());

        MenuItem item1 = new MenuItem();
        item1.setName("Dosa");
        item1.setDescription("Crispy Dosa");
        item1.setPrice(new BigDecimal("110"));
        item1.setPreparationTime(5);
        item1.setCategory("");
        item1.setFoodType(FoodType.VEGETARIAN);
        item1.setAvailable(true);
        item1.setCreatedAt(Instant.now());
        item1.setId(UUID.randomUUID());


        menu.getItems().add(item1);

        Mockito.when(menuRepository.findById(menuId))
                .thenReturn(Optional.of(menu));

        List<MenuItemSummaryResponse> result = service.listItems(menuId);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void listItems_menuNotFound() {

        Mockito.when(menuRepository.findById(Mockito.any()))
                .thenReturn(Optional.empty());

        RestoRestaurantException ex = assertThrows(
                RestoRestaurantException.class,
                () -> service.listItems(UUID.randomUUID())
        );

        assertEquals("Menu not found", ex.getMessage());
    }

}
