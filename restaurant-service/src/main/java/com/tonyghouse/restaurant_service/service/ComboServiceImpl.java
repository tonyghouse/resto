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

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ComboServiceImpl implements ComboService {

    private final ComboRepository comboRepository;
    private final MenuItemRepository menuItemRepository;
    private final Clock clock;

    @Override
    public ComboResponse create(CreateComboRequest request) {
        Combo combo = new Combo();
        combo.setName(request.getName());
        combo.setDescription(request.getDescription());
        combo.setComboPrice(request.getComboPrice());
        combo.setCreatedAt(Instant.now(clock));

        return ComboMapper.toResponse(comboRepository.save(combo));
    }

    @Override
    public ComboResponse get(UUID comboId) {
        return comboRepository.findById(comboId)
                .map(ComboMapper::toResponse)
                .orElseThrow(() -> new RestoRestaurantException("Combo not found", HttpStatus.NOT_FOUND));
    }

    @Override
    public List<ComboResponse> getAll() {
        return comboRepository.findAll()
                .stream()
                .map(ComboMapper::toResponse)
                .toList();
    }

    @Override
    public ComboResponse update(UUID comboId, UpdateComboRequest request) {
        Combo combo = comboRepository.findById(comboId)
                .orElseThrow(() -> new RestoRestaurantException("Combo not found", HttpStatus.NOT_FOUND));

        combo.setName(request.getName());
        combo.setDescription(request.getDescription());
        combo.setComboPrice(request.getComboPrice());

        return ComboMapper.toResponse(comboRepository.save(combo));
    }

    @Override
    public ComboResponse updateStatus(UUID comboId, boolean active) {
        Combo combo = comboRepository.findById(comboId)
                .orElseThrow(() -> new RestoRestaurantException("Combo not found", HttpStatus.NOT_FOUND));

        combo.setActive(active);
        return ComboMapper.toResponse(comboRepository.save(combo));
    }

    @Override
    public void addItem(UUID comboId, UUID itemId) {
        Combo combo = comboRepository.findById(comboId)
                .orElseThrow(() -> new RestoRestaurantException("Combo not found", HttpStatus.NOT_FOUND));

        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new RestoRestaurantException("Menu item not found", HttpStatus.NOT_FOUND));

        combo.getItems().add(item);
        comboRepository.save(combo);
    }

    @Override
    public void removeItem(UUID comboId, UUID itemId) {
        Combo combo = comboRepository.findById(comboId)
                .orElseThrow(() -> new RestoRestaurantException("Combo not found", HttpStatus.NOT_FOUND));

        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new RestoRestaurantException("Menu item not found", HttpStatus.NOT_FOUND));

        combo.getItems().remove(item);
        comboRepository.save(combo);
    }


}
