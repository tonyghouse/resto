package com.tonyghouse.restaurant_service.service;


import com.tonyghouse.restaurant_service.dto.ComboResponse;
import com.tonyghouse.restaurant_service.dto.CreateComboRequest;
import com.tonyghouse.restaurant_service.dto.UpdateComboRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ComboService {

    ComboResponse create(CreateComboRequest request);

    ComboResponse get(UUID comboId);

    Page<ComboResponse> getAll(Pageable pageable);

    ComboResponse update(UUID comboId, UpdateComboRequest request);

    ComboResponse updateStatus(UUID comboId, boolean active);

    void addItem(UUID comboId, UUID itemId);

    void removeItem(UUID comboId, UUID itemId);
}
