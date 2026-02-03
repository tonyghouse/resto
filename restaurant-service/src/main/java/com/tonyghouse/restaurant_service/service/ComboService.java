package com.tonyghouse.restaurant_service.service;


import com.tonyghouse.restaurant_service.dto.ComboResponse;
import com.tonyghouse.restaurant_service.dto.CreateComboRequest;
import com.tonyghouse.restaurant_service.dto.UpdateComboRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ComboService {

    ComboResponse createCombo(CreateComboRequest request);

    ComboResponse getCombo(UUID comboId);

    Page<ComboResponse> getCombos(Pageable pageable);

    ComboResponse updateCombo(UUID comboId, UpdateComboRequest request);

    ComboResponse updateComboStatus(UUID comboId, boolean active);

    void addItemToCombo(UUID comboId, UUID itemId);

    void removeItemFromCombo(UUID comboId, UUID itemId);
}
