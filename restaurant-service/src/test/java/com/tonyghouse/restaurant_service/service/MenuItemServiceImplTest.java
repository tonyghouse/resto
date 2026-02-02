package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.constants.FoodType;
import com.tonyghouse.restaurant_service.dto.CreateMenuItemRequest;
import com.tonyghouse.restaurant_service.dto.MenuItemResponse;
import com.tonyghouse.restaurant_service.dto.UpdateMenuItemRequest;
import com.tonyghouse.restaurant_service.entity.MenuItem;
import com.tonyghouse.restaurant_service.exception.RestoRestaurantException;
import com.tonyghouse.restaurant_service.repo.MenuItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MenuItemServiceImplTest {

    @Mock
    MenuItemRepository repository;

    @Mock
    Clock clock;

    @InjectMocks
    MenuItemServiceImpl service;

    @BeforeEach
    void setup() {
        Mockito.when(clock.instant())
                .thenReturn(Instant.parse("2025-01-01T00:00:00Z"));
    }

    @Test
    void create_MenuItem_success() {
        CreateMenuItemRequest request = new CreateMenuItemRequest();
        request.setName("Burger");
        request.setDescription("Cheese burger");
        request.setPrice(new BigDecimal("199.99"));
        request.setPreparationTime(10);
        request.setCategory("FAST_FOOD");
        request.setFoodType(FoodType.VEGETARIAN);

        ArgumentCaptor<MenuItem> captor = ArgumentCaptor.forClass(MenuItem.class);

        Mockito.when(repository.save(Mockito.any(MenuItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MenuItemResponse response = service.createMenuItem(request);

        Mockito.verify(repository).save(captor.capture());

        MenuItem saved = captor.getValue();

        assertNotNull(saved.getId());
        assertEquals("Burger", saved.getName());
        assertEquals("Cheese burger", saved.getDescription());
        assertEquals(new BigDecimal("199.99"), saved.getPrice());
        assertEquals(10, saved.getPreparationTime());
        assertEquals("FAST_FOOD", saved.getCategory());
        assertEquals(FoodType.VEGETARIAN, saved.getFoodType());

        assertTrue(saved.getAvailable());
        assertEquals(Instant.parse("2025-01-01T00:00:00Z"), saved.getCreatedAt());

        assertEquals("Burger", response.getName());
    }


    @Test
    void get_MenuItem_success() {
        UUID id = UUID.randomUUID();
        MenuItem item = new MenuItem();
        item.setId(id);
        item.setName("Item");

        Mockito.when(repository.findById(id))
                .thenReturn(Optional.of(item));

        MenuItemResponse res = service.getMenuItem(id);

        assertEquals("Item", res.getName());
    }

    @Test
    void get_MenuItem_notFound() {
        Mockito.when(repository.findById(Mockito.any()))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.getMenuItem(UUID.randomUUID()));
    }

    @Test
    void getMenuItemAll_success() {
        List<MenuItem> entities = List.of(new MenuItem(), new MenuItem());
        Pageable pageable = PageRequest.of(0, 10);
        Page<MenuItem> page =
                new PageImpl<>(entities, pageable, entities.size());

        Mockito.when(repository.findAll(Mockito.any(Pageable.class)))
                .thenReturn(page);

        Page<MenuItemResponse> result = service.getMenuItems(pageable);

        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
    }


    @Test
    void update_MenuItem_success() {
        UUID id = UUID.randomUUID();

        MenuItem existing = new MenuItem();
        existing.setId(id);

        UpdateMenuItemRequest request = new UpdateMenuItemRequest();
        request.setName("Pizza");
        request.setDescription("Farmhouse");
        request.setPrice(new BigDecimal("299.50"));
        request.setPreparationTime(15);
        request.setCategory("MAIN");
        request.setFoodType(FoodType.NON_VEGETARIAN);

        Mockito.when(repository.findById(id))
                .thenReturn(Optional.of(existing));

        Mockito.when(repository.save(Mockito.any(MenuItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MenuItemResponse response = service.updateMenuItem(id, request);

        assertEquals("Pizza", existing.getName());
        assertEquals("Farmhouse", existing.getDescription());
        assertEquals(new BigDecimal("299.50"), existing.getPrice());
        assertEquals(15, existing.getPreparationTime());
        assertEquals("MAIN", existing.getCategory());
        assertEquals(FoodType.NON_VEGETARIAN, existing.getFoodType());

        Mockito.verify(repository).save(existing);

        assertEquals("Pizza", response.getName());
    }


    @Test
    void update_MenuItem_notFound() {
        Mockito.when(repository.findById(Mockito.any()))
                .thenReturn(Optional.empty());

        assertThrows(RestoRestaurantException.class,
                () -> service.updateMenuItem(UUID.randomUUID(), new UpdateMenuItemRequest()));
    }

    @Test
    void updateMenuItemAvailability_success() {
        UUID id = UUID.randomUUID();
        MenuItem item = new MenuItem();
        item.setId(id);
        item.setAvailable(true);

        Mockito.when(repository.findById(id))
                .thenReturn(Optional.of(item));

        Mockito.when(repository.save(Mockito.any(MenuItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MenuItemResponse res = service.updateMenuItemAvailability(id, false);
        assertFalse(item.getAvailable());
        Mockito.verify(repository).save(item);
        assertFalse(res.getAvailable());
    }

    @Test
    void updateMenuItemAvailability_notFound() {
        Mockito.when(repository.findById(Mockito.any()))
                .thenReturn(Optional.empty());

        assertThrows(
                RestoRestaurantException.class,
                () -> service.updateMenuItemAvailability(UUID.randomUUID(), true)
        );
    }

}
