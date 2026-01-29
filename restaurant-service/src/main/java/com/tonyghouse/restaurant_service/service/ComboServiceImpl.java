package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.dto.ComboResponse;
import com.tonyghouse.restaurant_service.dto.CreateComboRequest;
import com.tonyghouse.restaurant_service.dto.UpdateComboRequest;
import com.tonyghouse.restaurant_service.entity.Combo;
import com.tonyghouse.restaurant_service.entity.MenuItem;
import com.tonyghouse.restaurant_service.exception.RestoRestaurantException;
import com.tonyghouse.restaurant_service.mapper.ComboMapper;
import com.tonyghouse.restaurant_service.repo.ComboRepository;
import com.tonyghouse.restaurant_service.repo.MenuItemRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPool;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ComboServiceImpl implements ComboService {

    private static final int CACHE_TTL_SECONDS = 600; // 10 minutes

    private final ComboRepository comboRepository;
    private final MenuItemRepository menuItemRepository;
    private final JedisPool jedisPool;
    private final Clock clock;

    @Override
    public ComboResponse create(CreateComboRequest request) {

        Combo combo = new Combo();
        combo.setName(request.getName());
        combo.setDescription(request.getDescription());
        combo.setComboPrice(request.getComboPrice());
        combo.setCreatedAt(Instant.now(clock));

        Combo saved = comboRepository.save(combo);
        evictComboCache(saved.getId());

        return ComboMapper.toResponse(saved);
    }

    @Override
    public ComboResponse get(UUID comboId) {

        String cacheKey = "combo:" + comboId;

        try (var jedis = jedisPool.getResource()) {
            String cached = jedis.get(cacheKey);
            if (cached != null) {
                return ComboMapper.fromJson(cached);
            }
        }

        ComboResponse response = comboRepository.findById(comboId)
                .map(ComboMapper::toResponse)
                .orElseThrow(() ->
                        new RestoRestaurantException("Combo not found", HttpStatus.NOT_FOUND));

        try (var jedis = jedisPool.getResource()) {
            jedis.setex(
                    cacheKey,
                    CACHE_TTL_SECONDS,
                    ComboMapper.toJson(response)
            );
        }

        return response;
    }

    @Override
    public List<ComboResponse> getAll() {

        String cacheKey = "combo:all";

        try (var jedis = jedisPool.getResource()) {
            String cached = jedis.get(cacheKey);
            if (cached != null) {
                return ComboMapper.fromJsonList(cached);
            }
        }

        List<ComboResponse> responses = comboRepository.findAll()
                .stream()
                .map(ComboMapper::toResponse)
                .toList();

        try (var jedis = jedisPool.getResource()) {
            jedis.setex(
                    cacheKey,
                    CACHE_TTL_SECONDS,
                    ComboMapper.toJsonList(responses)
            );
        }

        return responses;
    }

    @Override
    public ComboResponse update(UUID comboId, UpdateComboRequest request) {

        Combo combo = comboRepository.findById(comboId)
                .orElseThrow(() ->
                        new RestoRestaurantException("Combo not found", HttpStatus.NOT_FOUND));

        combo.setName(request.getName());
        combo.setDescription(request.getDescription());
        combo.setComboPrice(request.getComboPrice());

        Combo saved = comboRepository.save(combo);
        evictComboCache(comboId);

        return ComboMapper.toResponse(saved);
    }

    @Override
    public ComboResponse updateStatus(UUID comboId, boolean active) {

        Combo combo = comboRepository.findById(comboId)
                .orElseThrow(() ->
                        new RestoRestaurantException("Combo not found", HttpStatus.NOT_FOUND));

        combo.setActive(active);

        Combo saved = comboRepository.save(combo);
        evictComboCache(comboId);

        return ComboMapper.toResponse(saved);
    }

    @Override
    public void addItem(UUID comboId, UUID itemId) {

        Combo combo = comboRepository.findById(comboId)
                .orElseThrow(() ->
                        new RestoRestaurantException("Combo not found", HttpStatus.NOT_FOUND));

        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() ->
                        new RestoRestaurantException("Menu item not found", HttpStatus.NOT_FOUND));

        combo.getItems().add(item);
        comboRepository.save(combo);

        evictComboCache(comboId);
    }

    @Override
    public void removeItem(UUID comboId, UUID itemId) {

        Combo combo = comboRepository.findById(comboId)
                .orElseThrow(() ->
                        new RestoRestaurantException("Combo not found", HttpStatus.NOT_FOUND));

        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() ->
                        new RestoRestaurantException("Menu item not found", HttpStatus.NOT_FOUND));

        combo.getItems().remove(item);
        comboRepository.save(combo);

        evictComboCache(comboId);
    }

    private void evictComboCache(UUID comboId) {
        try (var jedis = jedisPool.getResource()) {
            jedis.del("combo:" + comboId);
            jedis.del("combo:all");
        }
    }
}
