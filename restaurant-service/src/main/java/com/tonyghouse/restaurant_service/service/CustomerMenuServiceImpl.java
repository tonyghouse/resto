package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.constants.MenuType;
import com.tonyghouse.restaurant_service.dto.ComboSummaryResponse;
import com.tonyghouse.restaurant_service.dto.MenuWithItemsResponse;
import com.tonyghouse.restaurant_service.entity.Menu;
import com.tonyghouse.restaurant_service.exception.RestoRestaurantException;
import com.tonyghouse.restaurant_service.mapper.CustomerMenuMapper;
import com.tonyghouse.restaurant_service.repo.ComboRepository;
import com.tonyghouse.restaurant_service.repo.MenuRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CustomerMenuServiceImpl implements CustomerMenuService {

    private final MenuRepository menuRepository;
    private final ComboRepository comboRepository;
    private final Clock clock;

    @Override
    public MenuWithItemsResponse getActiveMenu(UUID branchId, String timezone) {
        log.info("Fetching active menu. branchId={} timezone={}", branchId, timezone);

        ZoneId userZone = (timezone != null && !timezone.isBlank())
                ? ZoneId.of(timezone)
                : ZoneOffset.UTC;
        log.debug("Resolved timezone for branchId={} -> {}", branchId, userZone);

        Instant nowUtc = Instant.now(clock);
        LocalTime userLocalTime = nowUtc.atZone(userZone).toLocalTime();
        log.debug("Current user local time for branchId={} is {}", branchId, userLocalTime);


        Menu activeMenu = menuRepository.findByBranchIdAndActiveTrue(branchId)
                .stream()
                .filter(menu ->
                        !userLocalTime.isBefore(menu.getValidFrom())
                                && !userLocalTime.isAfter(menu.getValidTo()))
                .findFirst()
                .orElseThrow(() ->
                        new RestoRestaurantException(
                                "No active menu for current time",
                                HttpStatus.NOT_FOUND));

        log.debug("Loaded active menus from DB for branchId={}", branchId);
        return CustomerMenuMapper.toMenuResponse(activeMenu, activeMenu.getItems());
    }


    @Override
    public MenuWithItemsResponse getMenuWithItems(
            UUID branchId,
            MenuType menuType) {
        log.info("Fetching menu by type. branchId={} menuType={}", branchId, menuType);
        Menu menu = menuRepository
                .findByBranchIdAndMenuTypeAndActiveTrue(branchId, menuType)
                .orElseThrow(() ->
                        new RestoRestaurantException("Menu not found", HttpStatus.NOT_FOUND));

        log.debug("Menu found. menuId={} itemsCount={}", menu.getId(), menu.getItems().size());
        return CustomerMenuMapper.toMenuResponse(menu, menu.getItems());
    }

    @Override
    public List<ComboSummaryResponse> getActiveCombos(UUID branchId) {
        log.info("Fetching active combos for branchId={}", branchId);

        return comboRepository.findByActiveTrueAndBranch_Id(branchId)
                .stream()
                .map(c -> CustomerMenuMapper.toComboResponse(c, c.getItems()))
                .toList();
    }


}
