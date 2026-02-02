package com.tonyghouse.restaurant_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tonyghouse.restaurant_service.constants.MenuType;
import com.tonyghouse.restaurant_service.dto.CreateMenuRequest;
import com.tonyghouse.restaurant_service.dto.MenuResponse;
import com.tonyghouse.restaurant_service.dto.UpdateMenuStatusRequest;
import com.tonyghouse.restaurant_service.service.MenuService;

import java.time.Clock;
import java.time.LocalTime;
import java.time.ZoneOffset;
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
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static java.time.ZoneOffset.UTC;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MenuController.class)
@Import(MenuControllerTest.TestSecurityConfig.class)
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MenuService menuService;

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
    void shouldCreateMenu() throws Exception {

        UUID branchId = UUID.randomUUID();

        CreateMenuRequest request = new CreateMenuRequest();
        request.setMenuType(MenuType.BREAKFAST);
        request.setValidFrom(LocalTime.now(ZoneOffset.UTC));
        request.setValidTo(LocalTime.now(ZoneOffset.UTC));


        MenuResponse response = new MenuResponse();
        response.setId(UUID.randomUUID());

        Mockito.when(menuService.createMenu(eq(branchId), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/branches/{branchId}/menus", branchId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetMenuByType() throws Exception {

        UUID branchId = UUID.randomUUID();

        MenuResponse response = new MenuResponse();

        Mockito.when(menuService.getMenuByType(branchId, MenuType.BREAKFAST))
                .thenReturn(response);

        mockMvc.perform(get("/api/branches/{branchId}/menus/{menuType}",
                        branchId, MenuType.BREAKFAST))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldListMenus() throws Exception {

        UUID branchId = UUID.randomUUID();

        List<MenuResponse> responses = Arrays.asList(
                new MenuResponse(),
                new MenuResponse()
        );

        Mockito.when(menuService.getMenusByBranch(branchId))
                .thenReturn(responses);

        mockMvc.perform(get("/api/branches/{branchId}/menus", branchId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateMenuStatus() throws Exception {

        UUID menuId = UUID.randomUUID();

        UpdateMenuStatusRequest request = new UpdateMenuStatusRequest();
        request.setActive(true);

        MenuResponse response = new MenuResponse();

        Mockito.when(menuService.updateMenuStatus(menuId, true))
                .thenReturn(response);

        mockMvc.perform(patch("/api/menus/{menuId}/status", menuId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenWithWrongRole() throws Exception {

        UUID branchId = UUID.randomUUID();

        mockMvc.perform(get("/api/branches/{branchId}/menus", branchId))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedWithoutLogin() throws Exception {

        UUID branchId = UUID.randomUUID();

        mockMvc.perform(get("/api/branches/{branchId}/menus", branchId))
                .andExpect(status().isUnauthorized());
    }
}
