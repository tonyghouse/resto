package com.tonyghouse.restaurant_service.controller;

import com.tonyghouse.restaurant_service.dto.*;
import com.tonyghouse.restaurant_service.service.BranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public BranchResponse createBranch(@Valid @RequestBody CreateBranchRequest request) {

        log.info("Create branch request received. name={}", request.getName());
        log.debug("CreateBranchRequest payload={}", request);

        BranchResponse response = branchService.createBranch(request);

        log.info("Branch created successfully. branchId={}, name={}",
                response.getId(), response.getName());

        return response;
    }


    @GetMapping("/{branchId}")
    @PreAuthorize("hasRole('ADMIN')")
    public BranchResponse getBranch(@PathVariable UUID branchId) {

        log.debug("Fetching branch. branchId={}", branchId);

        return branchService.getBranch(branchId);
    }


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<BranchResponse> listBranches(Pageable pageable) {

        log.debug("Listing branches with paging: {}", pageable);
        Page<BranchResponse> branches = branchService.getAllBranches(pageable);
        log.info("Branches fetched. count={}", branches.getTotalElements());

        return branches;
    }



    @PutMapping("/{branchId}")
    @PreAuthorize("hasRole('ADMIN')")
    public BranchResponse updateBranch(
            @PathVariable UUID branchId,
            @Valid @RequestBody UpdateBranchRequest request) {

        log.info("Updating branch. branchId={}", branchId);
        log.debug("UpdateBranchRequest payload={}", request);

        BranchResponse response = branchService.updateBranch(branchId, request);

        log.info("Branch updated successfully. branchId={}", branchId);

        return response;
    }


    @DeleteMapping("/{branchId}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteBranch(@PathVariable UUID branchId) {

        log.warn("Deleting branch. branchId={}", branchId);

        branchService.deleteBranch(branchId);

        log.warn("Branch deleted successfully. branchId={}", branchId);
    }
}
