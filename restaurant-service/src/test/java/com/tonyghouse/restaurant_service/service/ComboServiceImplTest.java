package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.dto.ComboResponse;
import com.tonyghouse.restaurant_service.dto.CreateComboRequest;
import com.tonyghouse.restaurant_service.dto.UpdateComboRequest;
import com.tonyghouse.restaurant_service.entity.Combo;
import com.tonyghouse.restaurant_service.entity.MenuItem;
import com.tonyghouse.restaurant_service.exception.RestoRestaurantException;
import com.tonyghouse.restaurant_service.repo.ComboRepository;
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
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ComboServiceImplTest {

    @Mock
    ComboRepository comboRepository;

    @Mock
    MenuItemRepository menuItemRepository;

    @Mock
    JedisPool jedisPool;

    @Mock
    Jedis jedis;

    @Mock
    Clock clock;

    @InjectMocks
    ComboServiceImpl comboService;

    @BeforeEach
    void setup() {
        lenient().when(jedisPool.getResource()).thenReturn(jedis);
        lenient().when(clock.instant()).thenReturn(Instant.parse("2025-01-01T00:00:00Z"));
    }

    @Test
    void create() {
        CreateComboRequest req = new CreateComboRequest();
        req.setName("C");
        req.setDescription("D");
        req.setComboPrice(BigDecimal.valueOf(100.0));

        Mockito.when(comboRepository.save(Mockito.any()))
                .thenAnswer(i -> i.getArgument(0));

        ComboResponse res = comboService.create(req);

        assertEquals("C", res.getName());

    }

    @Test
    void get_cacheHit() {
        UUID id = UUID.randomUUID();
        Mockito.when(jedis.get("combo:" + id))
                .thenReturn("{\"id\":\"" + id + "\",\"name\":\"C\"}");

        ComboResponse res = comboService.get(id);

        assertEquals(id, res.getId());
    }


    @Test
    void get_notFound() {
        UUID id = UUID.randomUUID();
        Mockito.when(jedis.get("combo:" + id)).thenReturn(null);
        Mockito.when(comboRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RestoRestaurantException.class, () -> comboService.get(id));
    }


    @Test
    void update() {
        UUID id = UUID.randomUUID();
        Combo combo = new Combo();
        combo.setId(id);

        UpdateComboRequest req = new UpdateComboRequest();
        req.setName("N");
        req.setDescription("D");
        req.setComboPrice(BigDecimal.valueOf(50.0));

        Mockito.when(comboRepository.findById(id)).thenReturn(Optional.of(combo));
        Mockito.when(comboRepository.save(combo)).thenReturn(combo);

        ComboResponse res = comboService.update(id, req);

        assertEquals("N", res.getName());
        Mockito.verify(jedis).del("combo:" + id);
    }

    @Test
    void update_notFound() {
        UUID id = UUID.randomUUID();
        Mockito.when(comboRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RestoRestaurantException.class,
                () -> comboService.update(id, new UpdateComboRequest()));
    }

    @Test
    void updateStatus() {
        UUID id = UUID.randomUUID();
        Combo combo = new Combo();
        combo.setId(id);

        Mockito.when(comboRepository.findById(id)).thenReturn(Optional.of(combo));
        Mockito.when(comboRepository.save(combo)).thenReturn(combo);

        ComboResponse res = comboService.updateStatus(id, true);

        assertTrue(res.isActive());
        Mockito.verify(jedis).del("combo:" + id);
    }

    @Test
    void addItem() {
        UUID comboId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        Combo combo = new Combo();
        MenuItem item = new MenuItem();

        Mockito.when(comboRepository.findById(comboId)).thenReturn(Optional.of(combo));
        Mockito.when(menuItemRepository.findById(itemId)).thenReturn(Optional.of(item));

        comboService.addItem(comboId, itemId);

        Mockito.verify(comboRepository).save(combo);
        Mockito.verify(jedis).del("combo:" + comboId);
    }

    @Test
    void removeItem() {
        UUID comboId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        Combo combo = new Combo();
        MenuItem item = new MenuItem();
        combo.getItems().add(item);

        Mockito.when(comboRepository.findById(comboId)).thenReturn(Optional.of(combo));
        Mockito.when(menuItemRepository.findById(itemId)).thenReturn(Optional.of(item));

        comboService.removeItem(comboId, itemId);

        Mockito.verify(comboRepository).save(combo);
        Mockito.verify(jedis).del("combo:" + comboId);
    }
}
