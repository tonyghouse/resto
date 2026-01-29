package com.tonyghouse.restaurant_service.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class ComboSummaryResponse {

    private UUID id;
    private String name;
    private String description;
    private BigDecimal comboPrice;
    private List<UUID> itemIds;
}
