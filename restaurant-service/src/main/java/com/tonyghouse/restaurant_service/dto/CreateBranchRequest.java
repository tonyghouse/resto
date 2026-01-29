package com.tonyghouse.restaurant_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateBranchRequest {

    @NotBlank
    private String name;

    private String location;
}
