package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.dto.ComboResponse;
import com.tonyghouse.restaurant_service.dto.CreateComboRequest;
import com.tonyghouse.restaurant_service.dto.UpdateComboRequest;
import com.tonyghouse.restaurant_service.entity.Branch;
import com.tonyghouse.restaurant_service.entity.Combo;
import com.tonyghouse.restaurant_service.entity.MenuItem;
import com.tonyghouse.restaurant_service.exception.RestoRestaurantException;
import com.tonyghouse.restaurant_service.mapper.ComboMapper;
import com.tonyghouse.restaurant_service.repo.BranchRepository;
import com.tonyghouse.restaurant_service.repo.ComboRepository;
import com.tonyghouse.restaurant_service.repo.MenuItemRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPool;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ComboServiceImpl implements ComboService {

    private static final int CACHE_TTL_SECONDS = 600; // 10 minutes

    private final ComboRepository comboRepository;
    private final BranchRepository branchRepository;
    private final MenuItemRepository menuItemRepository;
    private final JedisPool jedisPool;
    private final Clock clock;

    @Override
    public ComboResponse createCombo(CreateComboRequest request) {
        log.info("Creating combo. branchId={} name={} price={}",
                request.getBranchId(), request.getName(), request.getComboPrice());

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() ->
                        new RestoRestaurantException("Branch not found", HttpStatus.NOT_FOUND));
        log.debug("Branch found for combo creation. branchId={}", branch.getId());

        if (comboRepository.existsByBranch_IdAndName(request.getBranchId(), request.getName())) {
            throw new RestoRestaurantException(
                    "Combo already exists with name in branch: " + request.getName(),
                    HttpStatus.BAD_REQUEST
            );
        }

        Combo combo = new Combo();
        combo.setBranch(branch);
        combo.setName(request.getName());
        combo.setDescription(request.getDescription());
        combo.setComboPrice(request.getComboPrice());
        combo.setCreatedAt(Instant.now(clock));

        Combo saved = comboRepository.save(combo);
        log.info("Combo created successfully. comboId={}", saved.getId());

        evictComboCache(saved.getId());
        log.debug("Cache evicted for comboId={}", saved.getId());


        return ComboMapper.toResponse(saved);
    }

    @Override
    public ComboResponse getCombo(UUID comboId) {
        log.debug("Fetching combo. comboId={}", comboId);

        String cacheKey = "combo:" + comboId;

        log.debug("Checking cache for comboId={}", comboId);
        try (var jedis = jedisPool.getResource()) {
            String cached = jedis.get(cacheKey);
            if (cached != null) {
                log.info("Cache HIT for comboId={}", comboId);
                return ComboMapper.fromJson(cached);
            }
        }
        log.info("Cache MISS for comboId={}, loading from DB", comboId);

        ComboResponse response = comboRepository.findById(comboId)
                .map(ComboMapper::toResponse)
                .orElseThrow(() ->
                        new RestoRestaurantException("Combo not found", HttpStatus.NOT_FOUND));

        log.debug("Combo loaded from DB. comboId={}", comboId);

        try (var jedis = jedisPool.getResource()) {
            jedis.setex(
                    cacheKey,
                    CACHE_TTL_SECONDS,
                    ComboMapper.toJson(response)
            );
            log.debug("Combo cached for {} seconds. comboId={}", CACHE_TTL_SECONDS, comboId);
        }

        return response;
    }

    @Override
    public Page<ComboResponse> getCombos(Pageable pageable) {
        log.debug("Fetching combos page. page={} size={}",
                pageable.getPageNumber(), pageable.getPageSize());
        return comboRepository.findAll(pageable)
                .map(ComboMapper::toResponse);
    }


    @Override
    public ComboResponse updateCombo(UUID comboId, UpdateComboRequest request) {
        log.info("Updating combo. comboId={}", comboId);

        Combo combo = comboRepository.findById(comboId)
                .orElseThrow(() ->
                        new RestoRestaurantException("Combo not found", HttpStatus.NOT_FOUND));
        //No branch update is allowed after creation of combo

        log.debug("Current combo loaded for update. comboId={}", comboId);
        combo.setName(request.getName());
        combo.setDescription(request.getDescription());
        combo.setComboPrice(request.getComboPrice());

        Combo saved = comboRepository.save(combo);
        log.info("Combo updated successfully. comboId={}", comboId);
        evictComboCache(comboId);

        return ComboMapper.toResponse(saved);
    }

    @Override
    public ComboResponse updateComboStatus(UUID comboId, boolean active) {
        log.info("Updating combo status. comboId={} active={}", comboId, active);
        Combo combo = comboRepository.findById(comboId)
                .orElseThrow(() ->
                        new RestoRestaurantException("Combo not found", HttpStatus.NOT_FOUND));

        combo.setActive(active);

        Combo saved = comboRepository.save(combo);
        log.info("Combo status updated. comboId={} active={}", comboId, active);

        evictComboCache(comboId);
        return ComboMapper.toResponse(saved);
    }

    @Override
    public void addItemToCombo(UUID comboId, UUID itemId) {
        log.info("Adding item to combo. comboId={} itemId={}", comboId, itemId);

        Combo combo = comboRepository.findById(comboId)
                .orElseThrow(() ->
                        new RestoRestaurantException("Combo not found", HttpStatus.NOT_FOUND));

        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() ->
                        new RestoRestaurantException("Menu item not found", HttpStatus.NOT_FOUND));
        log.debug("Combo and item found. comboId={} itemId={}", comboId, itemId);

        combo.getItems().add(item);
        comboRepository.save(combo);
        log.info("Item added to combo successfully. comboId={} itemId={}", comboId, itemId);

        evictComboCache(comboId);
    }

    @Override
    public void removeItemFromCombo(UUID comboId, UUID itemId) {
        log.info("Removing item from combo. comboId={} itemId={}", comboId, itemId);

        Combo combo = comboRepository.findById(comboId)
                .orElseThrow(() ->
                        new RestoRestaurantException("Combo not found", HttpStatus.NOT_FOUND));

        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() ->
                        new RestoRestaurantException("Menu item not found", HttpStatus.NOT_FOUND));

        combo.getItems().remove(item);
        comboRepository.save(combo);
        log.info("Item removed from combo successfully. comboId={} itemId={}", comboId, itemId);

        evictComboCache(comboId);
    }

    private void evictComboCache(UUID comboId) {
        try (var jedis = jedisPool.getResource()) {
            jedis.del("combo:" + comboId);
        }
        log.debug("Cache evicted for comboId={}", comboId);

    }
}
