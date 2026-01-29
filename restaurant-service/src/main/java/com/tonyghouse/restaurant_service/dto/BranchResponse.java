package com.tonyghouse.restaurant_service.dto;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class BranchResponse {

    private UUID id;
    private String name;
    private String location;
    private Instant createdAt;

}
