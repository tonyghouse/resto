package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.constants.MenuType;
import com.tonyghouse.restaurant_service.dto.CreateMenuRequest;
import com.tonyghouse.restaurant_service.dto.MenuResponse;
import com.tonyghouse.restaurant_service.entity.Branch;
import com.tonyghouse.restaurant_service.entity.Menu;
import com.tonyghouse.restaurant_service.exception.RestoRestaurantException;
import com.tonyghouse.restaurant_service.mapper.MenuMapper;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

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
        verify(jedis).del("menus:branch:" + branchId);
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
    void createMenu_branchNotFound() {
        UUID branchId = UUID.randomUUID();

        CreateMenuRequest req = new CreateMenuRequest();
        req.setMenuType(MenuType.BREAKFAST);

        Mockito.when(branchRepository.findById(branchId))
                .thenReturn(Optional.empty());

        RestoRestaurantException ex =
                assertThrows(RestoRestaurantException.class,
                        () -> service.createMenu(branchId, req));

        assertEquals("Branch not found", ex.getMessage());
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
        verify(jedis).del(
                "menu:" + branchId + ":" + MenuType.BREAKFAST,
                "menus:branch:" + branchId
        );
    }

    @Test
    void getMenuByType_cacheHit_returnsFromCache() {
        UUID branchId = UUID.randomUUID();
        MenuType type = MenuType.BREAKFAST;

        String key = "menu:" + branchId + ":" + type;

        MenuResponse cached = new MenuResponse();
        cached.setMenuType(type);

        Mockito.when(jedis.get(key))
                .thenReturn(MenuMapper.toJson(cached));

        MenuResponse res = service.getMenuByType(branchId, type);

        assertEquals(type, res.getMenuType());

        verify(menuRepository, Mockito.never())
                .findByBranch_IdAndMenuTypeAndActiveTrue(Mockito.any(), Mockito.any());
    }


    @Test
    void getMenuByType_cacheMiss_dbHit_setsCache() {
        UUID branchId = UUID.randomUUID();
        MenuType type = MenuType.LUNCH;

        String key = "menu:" + branchId + ":" + type;

        Branch branch = new Branch();
        branch.setId(branchId);

        Menu menu = new Menu();
        menu.setBranch(branch);
        menu.setMenuType(type);
        menu.setActive(true);

        Mockito.when(jedis.get(key)).thenReturn(null);

        Mockito.when(menuRepository
                        .findByBranch_IdAndMenuTypeAndActiveTrue(branchId, type))
                .thenReturn(Optional.of(menu));

        MenuResponse res = service.getMenuByType(branchId, type);

        assertEquals(type, res.getMenuType());

        verify(jedis)
                .setex(eq(key), eq(300L), anyString());

    }


    @Test
    void getMenuByType_notFound_throwsException() {
        UUID branchId = UUID.randomUUID();
        MenuType type = MenuType.DINNER;

        String key = "menu:" + branchId + ":" + type;

        Mockito.when(jedis.get(key)).thenReturn(null);

        Mockito.when(menuRepository
                        .findByBranch_IdAndMenuTypeAndActiveTrue(branchId, type))
                .thenReturn(Optional.empty());

        RestoRestaurantException ex =
                assertThrows(RestoRestaurantException.class,
                        () -> service.getMenuByType(branchId, type));

        assertEquals("Menu not found", ex.getMessage());
    }


    @Test
    void getMenusByBranch_cacheHit_returnsFromCache() {
        UUID branchId = UUID.randomUUID();
        String key = "menus:branch:" + branchId;

        List<MenuResponse> cachedMenus = List.of(new MenuResponse(), new MenuResponse());

        Mockito.when(jedis.get(key))
                .thenReturn(MenuMapper.listToJson(cachedMenus));

        List<MenuResponse> result = service.getMenusByBranch(branchId);

        assertEquals(2, result.size());
        Mockito.verify(menuRepository, Mockito.never())
                .findAllByBranch_Id(Mockito.any());
    }

    @Test
    void getMenusByBranch_cacheMiss_dbHit_setsCache() {
        UUID branchId = UUID.randomUUID();
        String key = "menus:branch:" + branchId;

        Branch branch = new Branch();
        branch.setId(branchId);

        Menu m1 = new Menu();
        m1.setBranch(branch);

        Menu m2 = new Menu();
        m2.setBranch(branch);

        Mockito.when(jedis.get(key)).thenReturn(null);

        Mockito.when(menuRepository.findAllByBranch_Id(branchId))
                .thenReturn(List.of(m1, m2));

        List<MenuResponse> result = service.getMenusByBranch(branchId);

        assertEquals(2, result.size());

        Mockito.verify(menuRepository)
                .findAllByBranch_Id(branchId);

        Mockito.verify(jedis)
                .setex(Mockito.eq(key), Mockito.eq(300L), Mockito.anyString());
    }


    @Test
    void updateMenuStatus_menuNotFound_throwsException() {
        UUID menuId = UUID.randomUUID();

        Mockito.when(menuRepository.findById(menuId))
                .thenReturn(Optional.empty());

        RestoRestaurantException ex =
                assertThrows(RestoRestaurantException.class,
                        () -> service.updateMenuStatus(menuId, true));

        assertEquals("Menu not found", ex.getMessage());
        Mockito.verify(jedisPool, Mockito.never()).getResource();
    }





}
