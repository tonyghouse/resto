package com.tonyghouse.restaurant_service.controller;

import com.tonyghouse.restaurant_service.dto.*;
import com.tonyghouse.restaurant_service.service.BranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public BranchResponse createBranch(@Valid @RequestBody CreateBranchRequest request) {
        return branchService.createBranch(request);
    }

    @GetMapping("/{branchId}")
    @PreAuthorize("hasRole('ADMIN')")
    public BranchResponse getBranch(@PathVariable UUID branchId) {
        return branchService.getBranch(branchId);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<BranchResponse> listBranches() {
        return branchService.getAllBranches();
    }

    @PutMapping("/{branchId}")
    @PreAuthorize("hasRole('ADMIN')")
    public BranchResponse updateBranch(@PathVariable UUID branchId, @Valid @RequestBody UpdateBranchRequest request) {
        return branchService.updateBranch(branchId, request);
    }

    @DeleteMapping("/{branchId}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteBranch(@PathVariable UUID branchId) {
        branchService.deleteBranch(branchId);
    }
}
