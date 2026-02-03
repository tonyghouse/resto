package com.tonyghouse.restaurant_service.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class ComboResponse {

    private UUID id;
    private String name;
    private String branchId;
    private String description;
    private BigDecimal comboPrice;
    private boolean active;
    private Instant createdAt;
    private List<UUID> itemIds;
}
