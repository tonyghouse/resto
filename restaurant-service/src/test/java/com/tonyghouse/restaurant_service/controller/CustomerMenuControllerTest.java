package com.tonyghouse.restaurant_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tonyghouse.restaurant_service.constants.MenuType;
import com.tonyghouse.restaurant_service.dto.ComboSummaryResponse;
import com.tonyghouse.restaurant_service.dto.MenuWithItemsResponse;
import com.tonyghouse.restaurant_service.service.CustomerMenuService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerMenuController.class)
@Import(CustomerMenuControllerTest.TestSecurityConfig.class)
class CustomerMenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerMenuService service;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        public Clock clock() {
            return Clock.systemDefaultZone();
        }
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldGetActiveMenu() throws Exception {

        UUID branchId = UUID.randomUUID();

        MenuWithItemsResponse response = new MenuWithItemsResponse();

        Mockito.when(service.getActiveMenu(branchId, "Asia/Kolkata"))
                .thenReturn(response);

        mockMvc.perform(get("/api/branches/{branchId}/menus/active", branchId)
                        .header("X-Timezone", "Asia/Kolkata"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldGetMenuWithItems() throws Exception {

        UUID branchId = UUID.randomUUID();

        MenuWithItemsResponse response = new MenuWithItemsResponse();

        Mockito.when(service.getMenuWithItems(branchId, MenuType.BREAKFAST))
                .thenReturn(response);

        mockMvc.perform(get("/api/branches/{branchId}/menus/{type}/items",
                        branchId, MenuType.BREAKFAST))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldGetCombos() throws Exception {

        UUID branchId = UUID.randomUUID();

        List<ComboSummaryResponse> responses = Arrays.asList(
                new ComboSummaryResponse(),
                new ComboSummaryResponse()
        );

        Mockito.when(service.getActiveCombos(branchId))
                .thenReturn(responses);

        mockMvc.perform(get("/api/branches/{branchId}/combos", branchId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "GUEST")
    void shouldReturnForbiddenWithWrongRole() throws Exception {

        UUID branchId = UUID.randomUUID();

        mockMvc.perform(get("/api/branches/{branchId}/menus/active", branchId))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedWithoutLogin() throws Exception {

        UUID branchId = UUID.randomUUID();

        mockMvc.perform(get("/api/branches/{branchId}/menus/active", branchId))
                .andExpect(status().isUnauthorized());
    }
}
