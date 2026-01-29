package com.tonyghouse.restaurant_service.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class OrderItemRequest {

    private UUID itemId;   // menu item OR combo id
    private String itemType; // ITEM or COMBO
    private int quantity;
    private String specialNotes;
}
