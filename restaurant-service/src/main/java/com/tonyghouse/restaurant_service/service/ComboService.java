package com.tonyghouse.restaurant_service.service;


import com.tonyghouse.restaurant_service.dto.ComboResponse;
import com.tonyghouse.restaurant_service.dto.CreateComboRequest;
import com.tonyghouse.restaurant_service.dto.UpdateComboRequest;

import java.util.List;
import java.util.UUID;

public interface ComboService {

    ComboResponse create(CreateComboRequest request);

    ComboResponse get(UUID comboId);

    List<ComboResponse> getAll();

    ComboResponse update(UUID comboId, UpdateComboRequest request);

    ComboResponse updateStatus(UUID comboId, boolean active);

    void addItem(UUID comboId, UUID itemId);

    void removeItem(UUID comboId, UUID itemId);
}
