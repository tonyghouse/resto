package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.constants.MenuType;
import com.tonyghouse.restaurant_service.dto.ComboSummaryResponse;
import com.tonyghouse.restaurant_service.dto.MenuItemSummaryResponse;
import com.tonyghouse.restaurant_service.dto.MenuWithItemsResponse;
import com.tonyghouse.restaurant_service.entity.Menu;
import com.tonyghouse.restaurant_service.exception.RestoRestaurantException;
import com.tonyghouse.restaurant_service.mapper.CustomerMenuMapper;
import com.tonyghouse.restaurant_service.repo.ComboRepository;
import com.tonyghouse.restaurant_service.repo.MenuRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
public class CustomerMenuServiceImpl implements CustomerMenuService {

    private final MenuRepository menuRepository;
    private final ComboRepository comboRepository;
    private final Clock clock;

    @Override
    public MenuWithItemsResponse getActiveMenu(UUID branchId, String timezone) {

        ZoneId userZone = (timezone != null && !timezone.isBlank())
                ? ZoneId.of(timezone)
                : ZoneOffset.UTC;

        Instant nowUtc = Instant.now(clock);

        LocalTime userLocalTime = nowUtc.atZone(userZone).toLocalTime();

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

        return CustomerMenuMapper.toMenuResponse(activeMenu, activeMenu.getItems());
    }


    @Override
    public MenuWithItemsResponse getMenuWithItems(
            UUID branchId,
            MenuType menuType) {

        Menu menu = menuRepository
                .findByBranchIdAndMenuTypeAndActiveTrue(branchId, menuType)
                .orElseThrow(() ->
                        new RestoRestaurantException("Menu not found", HttpStatus.NOT_FOUND));


        return CustomerMenuMapper.toMenuResponse(menu, menu.getItems());
    }

    @Override
    public List<ComboSummaryResponse> getActiveCombos(UUID branchId) {
        // branchId kept for future extension
        return comboRepository.findByActiveTrue()
                .stream()
                .map(c -> CustomerMenuMapper.toComboResponse(c, c.getItems()))
                .toList();
    }


}
