package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.dto.ComboResponse;
import com.tonyghouse.restaurant_service.dto.CreateComboRequest;
import com.tonyghouse.restaurant_service.dto.UpdateComboRequest;
import com.tonyghouse.restaurant_service.entity.Branch;
import com.tonyghouse.restaurant_service.entity.Combo;
import com.tonyghouse.restaurant_service.entity.MenuItem;
import com.tonyghouse.restaurant_service.exception.RestoRestaurantException;
import com.tonyghouse.restaurant_service.repo.BranchRepository;
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
    BranchRepository branchRepository;

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
    void createCombo() {

        UUID branchId = UUID.randomUUID();

        CreateComboRequest req = new CreateComboRequest();
        req.setBranchId(branchId);
        req.setName("C");
        req.setDescription("D");
        req.setComboPrice(BigDecimal.valueOf(100.0));

        Branch branch = new Branch();
        branch.setId(branchId);

        Mockito.when(branchRepository.findById(branchId))
                .thenReturn(Optional.of(branch));

        Mockito.when(comboRepository.existsByBranch_IdAndName(branchId, "C"))
                .thenReturn(false);

        Mockito.when(comboRepository.save(Mockito.any(Combo.class)))
                .thenAnswer(inv -> {
                    Combo c = inv.getArgument(0);
                    c.setId(UUID.randomUUID()); // simulate DB generated id
                    return c;
                });

        ComboResponse res = comboService.createCombo(req);

        assertEquals("C", res.getName());
    }


    @Test
    void get_Combo_cacheHit() {
        UUID id = UUID.randomUUID();
        Mockito.when(jedis.get("combo:" + id))
                .thenReturn("{\"id\":\"" + id + "\",\"name\":\"C\"}");

        ComboResponse res = comboService.getCombo(id);

        assertEquals(id, res.getId());
    }


    @Test
    void get_Combo_notFound() {
        UUID id = UUID.randomUUID();
        Mockito.when(jedis.get("combo:" + id)).thenReturn(null);
        Mockito.when(comboRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RestoRestaurantException.class, () -> comboService.getCombo(id));
    }


    @Test
    void updateCombo() {
        UUID id = UUID.randomUUID();
        Combo combo = new Combo();
        combo.setId(id);

        UpdateComboRequest req = new UpdateComboRequest();
        req.setName("N");
        req.setDescription("D");
        req.setComboPrice(BigDecimal.valueOf(50.0));

        Mockito.when(comboRepository.findById(id)).thenReturn(Optional.of(combo));
        Mockito.when(comboRepository.save(combo)).thenReturn(combo);

        ComboResponse res = comboService.updateCombo(id, req);

        assertEquals("N", res.getName());
        Mockito.verify(jedis).del("combo:" + id);
    }

    @Test
    void update_Combo_notFound() {
        UUID id = UUID.randomUUID();
        Mockito.when(comboRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RestoRestaurantException.class,
                () -> comboService.updateCombo(id, new UpdateComboRequest()));
    }

    @Test
    void updateComboStatus() {
        UUID id = UUID.randomUUID();
        Combo combo = new Combo();
        combo.setId(id);

        Mockito.when(comboRepository.findById(id)).thenReturn(Optional.of(combo));
        Mockito.when(comboRepository.save(combo)).thenReturn(combo);

        ComboResponse res = comboService.updateComboStatus(id, true);

        assertTrue(res.isActive());
        Mockito.verify(jedis).del("combo:" + id);
    }

    @Test
    void addItemToCombo() {
        UUID comboId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        Combo combo = new Combo();
        MenuItem item = new MenuItem();

        Mockito.when(comboRepository.findById(comboId)).thenReturn(Optional.of(combo));
        Mockito.when(menuItemRepository.findById(itemId)).thenReturn(Optional.of(item));

        comboService.addItemToCombo(comboId, itemId);

        Mockito.verify(comboRepository).save(combo);
        Mockito.verify(jedis).del("combo:" + comboId);
    }

    @Test
    void removeItemFromCombo() {
        UUID comboId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        Combo combo = new Combo();
        MenuItem item = new MenuItem();
        combo.getItems().add(item);

        Mockito.when(comboRepository.findById(comboId)).thenReturn(Optional.of(combo));
        Mockito.when(menuItemRepository.findById(itemId)).thenReturn(Optional.of(item));

        comboService.removeItemFromCombo(comboId, itemId);

        Mockito.verify(comboRepository).save(combo);
        Mockito.verify(jedis).del("combo:" + comboId);
    }
}
