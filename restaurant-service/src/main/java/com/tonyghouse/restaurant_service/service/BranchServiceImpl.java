package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.dto.*;
import com.tonyghouse.restaurant_service.entity.Branch;
import com.tonyghouse.restaurant_service.exception.RestoRestaurantException;
import com.tonyghouse.restaurant_service.mapper.BranchMapper;
import com.tonyghouse.restaurant_service.repo.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final JedisPool jedisPool;
    private final Clock clock;

    private static final String BRANCH_CACHE_KEY = "branch:";

    @Override
    public BranchResponse createBranch(CreateBranchRequest request) {
        Branch branch = new Branch();
        branch.setId(UUID.randomUUID());
        branch.setName(request.getName());
        branch.setLocation(request.getLocation());
        branch.setCreatedAt(clock.instant());

        Branch saved = branchRepository.save(branch);
        return BranchMapper.mapToResponse(saved);
    }

    @Override
    public BranchResponse getBranch(UUID branchId) {

        try (Jedis jedis = jedisPool.getResource()) {
            String cachedName = jedis.get(BRANCH_CACHE_KEY + branchId);
            if (cachedName != null) {
                BranchResponse res = new BranchResponse();
                res.setId(branchId);
                res.setName(cachedName);
                return res;
            }
        }

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RestoRestaurantException("Branch not found", HttpStatus.INTERNAL_SERVER_ERROR));

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(BRANCH_CACHE_KEY + branchId, 300, branch.getName());
        }

        return BranchMapper.mapToResponse(branch);
    }

    @Override
    public List<BranchResponse> getAllBranches() {
        return branchRepository.findAll()
                .stream()
                .map(BranchMapper::mapToResponse)
                .toList();
    }

    @Override
    public BranchResponse updateBranch(UUID branchId, UpdateBranchRequest request) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RestoRestaurantException("Branch not found", HttpStatus.INTERNAL_SERVER_ERROR));

        branch.setName(request.getName());
        branch.setLocation(request.getLocation());

        Branch updated = branchRepository.save(branch);

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(BRANCH_CACHE_KEY + branchId);
        }

        return BranchMapper.mapToResponse(updated);
    }

    @Override
    public void deleteBranch(UUID branchId) {
        // hard delete
        if (!branchRepository.existsById(branchId)) {
            throw new RestoRestaurantException("Branch not found", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        branchRepository.deleteById(branchId);

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(BRANCH_CACHE_KEY + branchId);
        }
    }


}

