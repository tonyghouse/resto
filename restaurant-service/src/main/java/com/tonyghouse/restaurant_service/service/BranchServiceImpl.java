package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.dto.*;
import com.tonyghouse.restaurant_service.entity.Branch;
import com.tonyghouse.restaurant_service.exception.RestoRestaurantException;
import com.tonyghouse.restaurant_service.mapper.BranchMapper;
import com.tonyghouse.restaurant_service.repo.BranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.time.Clock;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final JedisPool jedisPool;
    private final Clock clock;

    private static final String BRANCH_CACHE_KEY = "branch:";

    @Override
    public BranchResponse createBranch(CreateBranchRequest request) {
        log.info("Creating branch. name={} location={}", request.getName(), request.getLocation());

        Branch branch = new Branch();
        branch.setId(UUID.randomUUID());
        branch.setName(request.getName());
        branch.setLocation(request.getLocation());
        branch.setCreatedAt(clock.instant());

        Branch saved = branchRepository.save(branch);
        log.info("Branch created successfully. branchId={}", saved.getId());

        return BranchMapper.mapToResponse(saved);
    }

    @Override
    public BranchResponse getBranch(UUID branchId) {
        log.debug("Fetching branch. branchId={}", branchId);


        try (Jedis jedis = jedisPool.getResource()) {
            log.debug("Checking Cache for branchId={}", branchId);

            String cachedName = jedis.get(BRANCH_CACHE_KEY + branchId);
            if (cachedName != null) {
                log.info("Cache HIT for branchId={}", branchId);
                BranchResponse res = new BranchResponse();
                res.setId(branchId);
                res.setName(cachedName);
                return res;
            }
        }

        log.info("Cache MISS for branchId={}, loading from DB", branchId);

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RestoRestaurantException("Branch not found", HttpStatus.INTERNAL_SERVER_ERROR));
        log.debug("Branch loaded from DB. branchId={} name={}", branchId, branch.getName());


        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(BRANCH_CACHE_KEY + branchId, 300, branch.getName());
            log.debug("Branch cached in Cache for 300 seconds. branchId={}", branchId);

        }

        return BranchMapper.mapToResponse(branch);
    }

    @Override
    public Page<BranchResponse> getAllBranches(Pageable pageable) {
        log.debug("Fetching paginated branches. page={} size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        return branchRepository.findAll(pageable)
                .map(BranchMapper::mapToResponse);
    }


    @Override
    public BranchResponse updateBranch(UUID branchId, UpdateBranchRequest request) {
        log.info("Updating branch. branchId={}", branchId);

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RestoRestaurantException("Branch not found", HttpStatus.NOT_FOUND));
        log.debug("Current branch data loaded for update. branchId={}", branchId);

        branch.setName(request.getName());
        branch.setLocation(request.getLocation());

        Branch updated = branchRepository.save(branch);
        log.info("Branch updated successfully. branchId={}", branchId);


        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(BRANCH_CACHE_KEY + branchId);
            log.debug("Cache invalidated for branchId={}", branchId);
        }

        return BranchMapper.mapToResponse(updated);
    }

    @Override
    public void deleteBranch(UUID branchId) {
        log.info("Deleting branch. branchId={}", branchId);
        // hard delete
        if (!branchRepository.existsById(branchId)) {
            log.warn("Delete failed. Branch not found. branchId={}", branchId);
            throw new RestoRestaurantException("Branch not found", HttpStatus.NOT_FOUND);
        }

        branchRepository.deleteById(branchId);
        log.info("Branch deleted from DB. branchId={}", branchId);

        try (Jedis jedis = jedisPool.getResource()) {
            log.debug("Cache cleared for deleted branchId={}", branchId);
            jedis.del(BRANCH_CACHE_KEY + branchId);
        }
    }


}

