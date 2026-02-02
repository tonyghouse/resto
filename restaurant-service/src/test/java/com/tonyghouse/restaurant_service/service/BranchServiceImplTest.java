package com.tonyghouse.restaurant_service.service;


import com.tonyghouse.restaurant_service.dto.BranchResponse;
import com.tonyghouse.restaurant_service.dto.CreateBranchRequest;
import com.tonyghouse.restaurant_service.dto.UpdateBranchRequest;
import com.tonyghouse.restaurant_service.entity.Branch;
import com.tonyghouse.restaurant_service.exception.RestoRestaurantException;
import com.tonyghouse.restaurant_service.repo.BranchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BranchServiceImplTest {

    @Mock
    BranchRepository branchRepository;

    @Mock
    JedisPool jedisPool;

    @Mock
    Jedis jedis;

    @Mock
    Clock clock;



    @InjectMocks
    BranchServiceImpl branchService;

    @BeforeEach
    void setup() {
        Mockito.when(jedisPool.getResource()).thenReturn(jedis);
        Mockito.when(clock.instant()).thenReturn(Instant.parse("2025-01-01T00:00:00Z"));
    }


    @Test
    void createBranch() {
        CreateBranchRequest request = new CreateBranchRequest();
        request.setName("A");
        request.setLocation("L");

        Mockito.when(branchRepository.save(Mockito.any()))
                .thenAnswer(i -> i.getArgument(0));

        BranchResponse response = branchService.createBranch(request);

        assertEquals("A", response.getName());
    }

    @Test
    void getBranch_cacheHit() {
        UUID id = UUID.randomUUID();
        Mockito.when(jedis.get("branch:" + id)).thenReturn("Cached");

        BranchResponse response = branchService.getBranch(id);

        assertEquals("Cached", response.getName());
    }

    @Test
    void getBranch_cacheMiss() {
        UUID id = UUID.randomUUID();
        Branch branch = new Branch();
        branch.setId(id);
        branch.setName("DB");

        Mockito.when(jedis.get("branch:" + id)).thenReturn(null);
        Mockito.when(branchRepository.findById(id)).thenReturn(Optional.of(branch));

        BranchResponse response = branchService.getBranch(id);

        assertEquals("DB", response.getName());
        Mockito.verify(jedis).setex("branch:" + id, 300, "DB");
    }

    @Test
    void getBranch_notFound() {
        UUID id = UUID.randomUUID();
        Mockito.when(jedis.get("branch:" + id)).thenReturn(null);
        Mockito.when(branchRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RestoRestaurantException.class, () -> branchService.getBranch(id));
    }

    @Test
    void getAllBranches() {
        Branch b1 = new Branch();
        b1.setName("B1");
        Branch b2 = new Branch();
        b2.setName("B2");

        List<Branch> branches = List.of(b1, b2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Branch> page = new PageImpl<>(branches, pageable, branches.size());
        Mockito.when(branchRepository.findAll(Mockito.any(Pageable.class)))
                .thenReturn(page);
        Page<BranchResponse> result = branchService.getAllBranches(pageable);
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
    }

    @Test
    void updateBranch() {
        UUID id = UUID.randomUUID();
        Branch branch = new Branch();
        branch.setId(id);

        UpdateBranchRequest req = new UpdateBranchRequest();
        req.setName("N");
        req.setLocation("L");

        Mockito.when(branchRepository.findById(id)).thenReturn(Optional.of(branch));
        Mockito.when(branchRepository.save(branch)).thenReturn(branch);

        BranchResponse res = branchService.updateBranch(id, req);

        assertEquals("N", res.getName());
        Mockito.verify(jedis).del("branch:" + id);
    }

    @Test
    void updateBranch_notFound() {
        UUID id = UUID.randomUUID();
        Mockito.when(branchRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RestoRestaurantException.class,
                () -> branchService.updateBranch(id, new UpdateBranchRequest()));
    }

    @Test
    void deleteBranch() {
        UUID id = UUID.randomUUID();
        Mockito.when(branchRepository.existsById(id)).thenReturn(true);

        branchService.deleteBranch(id);

        Mockito.verify(branchRepository).deleteById(id);
        Mockito.verify(jedis).del("branch:" + id);
    }

    @Test
    void deleteBranch_notFound() {
        UUID id = UUID.randomUUID();
        Mockito.when(branchRepository.existsById(id)).thenReturn(false);

        assertThrows(RestoRestaurantException.class, () -> branchService.deleteBranch(id));
    }
}
