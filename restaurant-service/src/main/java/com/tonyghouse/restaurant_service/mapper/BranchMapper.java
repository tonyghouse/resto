package com.tonyghouse.restaurant_service.mapper;

import com.tonyghouse.restaurant_service.dto.BranchResponse;
import com.tonyghouse.restaurant_service.entity.Branch;

public class BranchMapper {
    public static BranchResponse mapToResponse(Branch branch) {
        BranchResponse response = new BranchResponse();
        response.setId(branch.getId());
        response.setName(branch.getName());
        response.setLocation(branch.getLocation());
        response.setCreatedAt(branch.getCreatedAt());
        return response;
    }
}
