package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.dto.BranchResponse;
import com.tonyghouse.restaurant_service.dto.CreateBranchRequest;
import com.tonyghouse.restaurant_service.dto.UpdateBranchRequest;

import java.util.List;
import java.util.UUID;

public interface BranchService {

    BranchResponse createBranch(CreateBranchRequest request);

    BranchResponse getBranch(UUID branchId);

    List<BranchResponse> getAllBranches();

    BranchResponse updateBranch(UUID branchId, UpdateBranchRequest request);

    void deleteBranch(UUID branchId);
}
