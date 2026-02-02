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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final BranchRepository branchRepository;
    private final JedisPool jedisPool;
    private final Clock clock;

    private static final String MENU_CACHE_KEY = "menu:";
    private static final String BRANCH_MENUS_KEY = "menus:branch:";

    @Override
    public MenuResponse createMenu(UUID branchId, CreateMenuRequest request) {

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() ->
                        new RestoRestaurantException("Branch not found", HttpStatus.NOT_FOUND));

        if (menuRepository.existsByBranch_IdAndMenuType(branchId, request.getMenuType())) {
            throw new RestoRestaurantException(
                    "Menu already exists for type: " + request.getMenuType(),
                    HttpStatus.BAD_REQUEST
            );
        }

        Menu menu = new Menu();
        menu.setBranch(branch);
        menu.setMenuType(request.getMenuType());
        menu.setValidFrom(request.getValidFrom());
        menu.setValidTo(request.getValidTo());
        menu.setActive(true);
        menu.setCreatedAt(Instant.now(clock));

        Menu saved = menuRepository.save(menu);

        // invalidate branch menu cache
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(BRANCH_MENUS_KEY + branchId);
        }

        return MenuMapper.toResponse(saved);
    }

    @Override
    public MenuResponse getMenuByType(UUID branchId, MenuType menuType) {

        String cacheKey = MENU_CACHE_KEY + branchId + ":" + menuType;

        try (Jedis jedis = jedisPool.getResource()) {
            String cached = jedis.get(cacheKey);
            if (cached != null) {
                return MenuMapper.fromJson(cached);
            }
        }

        Menu menu = menuRepository
                .findByBranch_IdAndMenuTypeAndActiveTrue(branchId, menuType)
                .orElseThrow(() ->
                        new RestoRestaurantException("Menu not found", HttpStatus.NOT_FOUND));

        MenuResponse response = MenuMapper.toResponse(menu);

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(cacheKey, 300, MenuMapper.toJson(response));
        }

        return response;
    }

    @Override
    public List<MenuResponse> getMenusByBranch(UUID branchId) {

        String cacheKey = BRANCH_MENUS_KEY + branchId;

        try (Jedis jedis = jedisPool.getResource()) {
            String cached = jedis.get(cacheKey);
            if (cached != null) {
                return MenuMapper.listFromJson(cached);
            }
        }

        List<MenuResponse> menus = menuRepository.findAllByBranch_Id(branchId)
                .stream()
                .map(MenuMapper::toResponse)
                .toList();

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(cacheKey, 300, MenuMapper.listToJson(menus));
        }

        return menus;
    }

    @Override
    public MenuResponse updateMenuStatus(UUID menuId, boolean active) {

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() ->
                        new RestoRestaurantException("Menu not found", HttpStatus.NOT_FOUND));

        menu.setActive(active);
        Menu updated = menuRepository.save(menu);

        // invalidate caches
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(
                    MENU_CACHE_KEY + menu.getBranch().getId() + ":" + menu.getMenuType(),
                    BRANCH_MENUS_KEY + menu.getBranch().getId()
            );
        }

        return MenuMapper.toResponse(updated);
    }
}
