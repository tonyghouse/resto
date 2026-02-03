package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.constants.MenuType;
import com.tonyghouse.restaurant_service.dto.ComboSummaryResponse;
import com.tonyghouse.restaurant_service.dto.MenuWithItemsResponse;
import com.tonyghouse.restaurant_service.entity.Combo;
import com.tonyghouse.restaurant_service.entity.Menu;
import com.tonyghouse.restaurant_service.exception.RestoRestaurantException;
import com.tonyghouse.restaurant_service.repo.ComboRepository;
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

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CustomerMenuServiceImplTest {

    @Mock
    MenuRepository menuRepository;

    @Mock
    ComboRepository comboRepository;

    @Mock
    Clock clock;

    @InjectMocks
    CustomerMenuServiceImpl service;

    @BeforeEach
    void setup() {
        Mockito.when(clock.instant())
                .thenReturn(Instant.parse("2025-01-01T10:00:00Z"));
    }

    @Test
    void getActiveMenu_success() {
        UUID branchId = UUID.randomUUID();

        Menu menu = new Menu();
        menu.setValidFrom(LocalTime.of(9, 0));
        menu.setValidTo(LocalTime.of(11, 0));

        Mockito.when(menuRepository.findByBranchIdAndActiveTrue(branchId))
                .thenReturn(List.of(menu));

        MenuWithItemsResponse res =
                service.getActiveMenu(branchId, ZoneOffset.UTC.getId());

        assertNotNull(res);
    }

    @Test
    void getActiveMenu_notFound() {
        UUID branchId = UUID.randomUUID();

        Menu menu = new Menu();
        menu.setValidFrom(LocalTime.of(11, 0));
        menu.setValidTo(LocalTime.of(12, 0));

        Mockito.when(menuRepository.findByBranchIdAndActiveTrue(branchId))
                .thenReturn(List.of(menu));

        assertThrows(RestoRestaurantException.class,
                () -> service.getActiveMenu(branchId, ZoneOffset.UTC.getId()));
    }

    @Test
    void getMenuWithItems_success() {
        UUID branchId = UUID.randomUUID();

        Menu menu = new Menu();

        Mockito.when(
                menuRepository.findByBranchIdAndMenuTypeAndActiveTrue(
                        branchId, MenuType.BREAKFAST))
                .thenReturn(Optional.of(menu));

        MenuWithItemsResponse res =
                service.getMenuWithItems(branchId, MenuType.BREAKFAST);

        assertNotNull(res);
    }

    @Test
    void getMenuWithItems_notFound() {
        UUID branchId = UUID.randomUUID();

        Mockito.when(
                menuRepository.findByBranchIdAndMenuTypeAndActiveTrue(
                        branchId, MenuType.DINNER))
                .thenReturn(Optional.empty());

        assertThrows(RestoRestaurantException.class,
                () -> service.getMenuWithItems(branchId, MenuType.DINNER));
    }

    @Test
    void getActiveCombos_success() {
        UUID branchId = UUID.randomUUID();
        Combo combo = new Combo();

        Mockito.when(comboRepository.findByActiveTrueAndBranch_Id(branchId))
                .thenReturn(List.of(combo));

        List<ComboSummaryResponse> res =
                service.getActiveCombos(branchId);

        assertEquals(1, res.size());
    }
}
