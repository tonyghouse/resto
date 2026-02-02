package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.dto.BranchResponse;
import com.tonyghouse.restaurant_service.dto.CreateBranchRequest;
import com.tonyghouse.restaurant_service.dto.UpdateBranchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface BranchService {

    BranchResponse createBranch(CreateBranchRequest request);

    BranchResponse getBranch(UUID branchId);

    Page<BranchResponse> getAllBranches(Pageable pageable);

    BranchResponse updateBranch(UUID branchId, UpdateBranchRequest request);

    void deleteBranch(UUID branchId);
}
