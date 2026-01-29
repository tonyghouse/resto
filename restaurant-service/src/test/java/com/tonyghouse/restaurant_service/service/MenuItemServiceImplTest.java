package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.dto.CreateMenuItemRequest;
import com.tonyghouse.restaurant_service.dto.MenuItemResponse;
import com.tonyghouse.restaurant_service.dto.UpdateMenuItemRequest;
import com.tonyghouse.restaurant_service.entity.MenuItem;
import com.tonyghouse.restaurant_service.exception.RestoRestaurantException;
import com.tonyghouse.restaurant_service.repo.MenuItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

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
    void get_success() {
        UUID id = UUID.randomUUID();
        MenuItem item = new MenuItem();
        item.setId(id);
        item.setName("Item");

        Mockito.when(repository.findById(id))
                .thenReturn(Optional.of(item));

        MenuItemResponse res = service.get(id);

        assertEquals("Item", res.getName());
    }

    @Test
    void get_notFound() {
        Mockito.when(repository.findById(Mockito.any()))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.get(UUID.randomUUID()));
    }

    @Test
    void getAll_success() {
        Mockito.when(repository.findAll())
                .thenReturn(List.of(new MenuItem(), new MenuItem()));

        List<MenuItemResponse> res = service.getAll();

        assertEquals(2, res.size());
    }

    @Test
    void updateAvailability_success() {
        UUID id = UUID.randomUUID();
        MenuItem item = new MenuItem();
        item.setAvailable(true);

        Mockito.when(repository.findById(id))
                .thenReturn(Optional.of(item));
        Mockito.when(repository.save(item))
                .thenReturn(item);

        MenuItemResponse res = service.updateAvailability(id, false);

    }

    @Test
    void update_notFound() {
        Mockito.when(repository.findById(Mockito.any()))
                .thenReturn(Optional.empty());

        assertThrows(RestoRestaurantException.class,
                () -> service.update(UUID.randomUUID(), new UpdateMenuItemRequest()));
    }
}
