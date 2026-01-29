package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.constants.MenuType;
import com.tonyghouse.restaurant_service.dto.CreateMenuRequest;
import com.tonyghouse.restaurant_service.dto.MenuResponse;
import com.tonyghouse.restaurant_service.entity.Branch;
import com.tonyghouse.restaurant_service.entity.Menu;
import com.tonyghouse.restaurant_service.exception.RestoRestaurantException;
import com.tonyghouse.restaurant_service.repo.BranchRepository;
import com.tonyghouse.restaurant_service.repo.MenuRepository;
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

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MenuServiceImplTest {

    @Mock
    MenuRepository menuRepository;

    @Mock
    BranchRepository branchRepository;

    @Mock
    JedisPool jedisPool;

    @Mock
    Jedis jedis;

    @Mock
    Clock clock;

    @InjectMocks
    MenuServiceImpl service;

    @BeforeEach
    void setup() {
        lenient().when(jedisPool.getResource()).thenReturn(jedis);
        lenient().when(clock.instant()).thenReturn(Instant.parse("2025-01-01T00:00:00Z"));
    }

    @Test
    void createMenu_success() {
        UUID branchId = UUID.randomUUID();
        Branch branch = new Branch();

        CreateMenuRequest req = new CreateMenuRequest();
        req.setMenuType(MenuType.BREAKFAST);

        Mockito.when(branchRepository.findById(branchId))
                .thenReturn(Optional.of(branch));
        Mockito.when(menuRepository.existsByBranch_IdAndMenuType(branchId, MenuType.BREAKFAST))
                .thenReturn(false);
        Mockito.when(menuRepository.save(Mockito.any()))
                .thenAnswer(i -> i.getArgument(0));

        MenuResponse res = service.createMenu(branchId, req);

        assertEquals(MenuType.BREAKFAST, res.getMenuType());
        Mockito.verify(jedis).del("menus:branch:" + branchId);
    }

    @Test
    void createMenu_duplicateType() {
        UUID branchId = UUID.randomUUID();
        Branch branch = new Branch();

        CreateMenuRequest req = new CreateMenuRequest();
        req.setMenuType(MenuType.LUNCH);

        Mockito.when(branchRepository.findById(branchId))
                .thenReturn(Optional.of(branch));
        Mockito.when(menuRepository.existsByBranch_IdAndMenuType(branchId, MenuType.LUNCH))
                .thenReturn(true);

        assertThrows(RestoRestaurantException.class,
                () -> service.createMenu(branchId, req));
    }

    @Test
    void updateMenuStatus_success() {
        UUID menuId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();

        Branch branch = new Branch();
        branch.setId(branchId);

        Menu menu = new Menu();
        menu.setId(menuId);
        menu.setBranch(branch);
        menu.setMenuType(MenuType.BREAKFAST);

        Mockito.when(menuRepository.findById(menuId))
                .thenReturn(Optional.of(menu));
        Mockito.when(menuRepository.save(menu))
                .thenReturn(menu);

        MenuResponse res = service.updateMenuStatus(menuId, false);

        assertFalse(res.isActive());
        Mockito.verify(jedis).del(
                "menu:" + branchId + ":" + MenuType.BREAKFAST,
                "menus:branch:" + branchId
        );
    }
}
