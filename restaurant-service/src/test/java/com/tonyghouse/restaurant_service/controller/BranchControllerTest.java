package com.tonyghouse.restaurant_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tonyghouse.restaurant_service.dto.BranchResponse;
import com.tonyghouse.restaurant_service.dto.CreateBranchRequest;
import com.tonyghouse.restaurant_service.dto.UpdateBranchRequest;
import com.tonyghouse.restaurant_service.service.BranchService;

import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BranchController.class)
@Import(BranchControllerTest.TestSecurityConfig.class)
class BranchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BranchService branchService;

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        public Clock clock() {
            return Clock.systemDefaultZone();
        }
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateBranch() throws Exception {

        CreateBranchRequest request = new CreateBranchRequest();
        request.setName("Resto Whitefield");
        request.setLocation("Whitefield");

        BranchResponse response = new BranchResponse();
        response.setId(UUID.randomUUID());

        Mockito.when(branchService.createBranch(any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/branches")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetBranch() throws Exception {

        UUID branchId = UUID.randomUUID();
        BranchResponse response = new BranchResponse();
        response.setId(branchId);

        Mockito.when(branchService.getBranch(branchId))
                .thenReturn(response);

        mockMvc.perform(get("/api/branches/{branchId}", branchId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldListBranches() throws Exception {
        BranchResponse response1 = new BranchResponse();
        BranchResponse response2 = new BranchResponse();
        List<BranchResponse> content = Arrays.asList(response1, response2);

        Page<BranchResponse> page =
                new PageImpl<>(content, PageRequest.of(0, 20), content.size());

        Mockito.when(branchService.getAllBranches(Mockito.any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/branches")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateBranch() throws Exception {

        UUID branchId = UUID.randomUUID();
        UpdateBranchRequest request = new UpdateBranchRequest();
        request.setName("Resto Whitefield");
        request.setLocation("Whitefield");
        BranchResponse response = new BranchResponse();
        response.setId(branchId);

        Mockito.when(branchService.updateBranch(eq(branchId), any()))
                .thenReturn(response);

        mockMvc.perform(put("/api/branches/{branchId}", branchId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteBranch() throws Exception {

        UUID branchId = UUID.randomUUID();

        mockMvc.perform(delete("/api/branches/{branchId}", branchId)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenWithWrongRole() throws Exception {

        UUID branchId = UUID.randomUUID();

        mockMvc.perform(get("/api/branches/{branchId}", branchId))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedWithoutLogin() throws Exception {

        UUID branchId = UUID.randomUUID();

        mockMvc.perform(get("/api/branches/{branchId}", branchId))
                .andExpect(status().isUnauthorized());
    }
}