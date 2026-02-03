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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final BranchRepository branchRepository;
    private final JedisPool jedisPool;
    private final Clock clock;

    private static final String MENU_CACHE_KEY = "menu:";
    private static final String BRANCH_MENUS_KEY = "menus:branch:";

    @Override
    public MenuResponse createMenu(UUID branchId, CreateMenuRequest request) {
        log.info("Creating menu. branchId={} menuType={} validFrom={} validTo={}",
                branchId, request.getMenuType(), request.getValidFrom(), request.getValidTo());

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() ->
                        new RestoRestaurantException("Branch not found", HttpStatus.NOT_FOUND));
        log.debug("Branch found for menu creation. branchId={}", branchId);

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
        log.info("Menu created successfully. menuId={} branchId={} menuType={}",
                saved.getId(), branchId, saved.getMenuType());


        // invalidate branch menu cache
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(BRANCH_MENUS_KEY + branchId);
            log.debug("Invalidated branch menus cache. branchId={}", branchId);

        }

        return MenuMapper.toResponse(saved);
    }

    @Override
    public MenuResponse getMenuByType(UUID branchId, MenuType menuType) {
        log.debug("Fetching menu by type. branchId={} menuType={}", branchId, menuType);

        String cacheKey = MENU_CACHE_KEY + branchId + ":" + menuType;
        log.debug("Checking cache for key={}", cacheKey);

        try (Jedis jedis = jedisPool.getResource()) {
            String cached = jedis.get(cacheKey);
            if (cached != null) {
                log.info("Cache HIT for menu. branchId={} menuType={}", branchId, menuType);
                return MenuMapper.fromJson(cached);
            }
        }

        log.info("Cache MISS for menu. branchId={} menuType={}, loading from DB", branchId, menuType);

        Menu menu = menuRepository
                .findByBranch_IdAndMenuTypeAndActiveTrue(branchId, menuType)
                .orElseThrow(() ->
                        new RestoRestaurantException("Menu not found", HttpStatus.NOT_FOUND));
        log.debug("Menu loaded from DB. menuId={}", menu.getId());

        MenuResponse response = MenuMapper.toResponse(menu);

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(cacheKey, 300, MenuMapper.toJson(response));
            log.debug("Menu cached for 300 seconds. key={}", cacheKey);
        }

        log.info("Returning menu. menuId={}", menu.getId());

        return response;
    }

    @Override
    public List<MenuResponse> getMenusByBranch(UUID branchId) {
        log.debug("Fetching all menus for branchId={}", branchId);

        String cacheKey = BRANCH_MENUS_KEY + branchId;
        log.debug("Checking branch menus cache. key={}", cacheKey);
        try (Jedis jedis = jedisPool.getResource()) {
            String cached = jedis.get(cacheKey);
            if (cached != null) {
                log.info("Cache HIT for branch menus. branchId={}", branchId);
                return MenuMapper.listFromJson(cached);
            }
        }

        log.info("Cache MISS for branch menus. branchId={}, loading from DB", branchId);

        List<MenuResponse> menus = menuRepository.findAllByBranch_Id(branchId)
                .stream()
                .map(MenuMapper::toResponse)
                .toList();

        log.debug("Loaded {} menus from DB for branchId={}", menus.size(), branchId);

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(cacheKey, 300, MenuMapper.listToJson(menus));
            log.debug("Branch menus cached for 300 seconds. branchId={}", branchId);

        }

        log.debug("Returning {} menus for branchId={}", menus.size(), branchId);

        return menus;
    }

    @Override
    public MenuResponse updateMenuStatus(UUID menuId, boolean active) {
        log.info("Updating menu status. menuId={} active={}", menuId, active);

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() ->
                        new RestoRestaurantException("Menu not found", HttpStatus.NOT_FOUND));

        log.debug("Menu loaded. menuId={} branchId={} type={}",
                menu.getId(), menu.getBranch().getId(), menu.getMenuType());


        menu.setActive(active);
        Menu updated = menuRepository.save(menu);
        log.info("Menu status updated successfully. menuId={} active={}", menuId, active);

        // invalidate caches
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(
                    MENU_CACHE_KEY + menu.getBranch().getId() + ":" + menu.getMenuType(),
                    BRANCH_MENUS_KEY + menu.getBranch().getId()
            );
            log.debug("Invalidated menu + branch caches for branchId={}", menu.getBranch().getId());
        }

        return MenuMapper.toResponse(updated);
    }
}
