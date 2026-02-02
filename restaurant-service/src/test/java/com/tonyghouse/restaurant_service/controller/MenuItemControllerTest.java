package com.tonyghouse.restaurant_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tonyghouse.restaurant_service.constants.FoodType;
import com.tonyghouse.restaurant_service.dto.*;
import com.tonyghouse.restaurant_service.service.MenuItemService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
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

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MenuItemController.class)
@Import(MenuItemControllerTest.TestSecurityConfig.class)
class MenuItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MenuItemService service;

    private final UUID ITEM_ID = UUID.randomUUID();

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        public Clock clock() {
            return Clock.systemDefaultZone();
        }
    }

    private MenuItemResponse mockResponse() {
        MenuItemResponse r = new MenuItemResponse();
        r.setId(ITEM_ID);
        r.setName("Masala Dosa");
        r.setDescription("Crispy dosa with masala");
        r.setPrice(new BigDecimal("60.00"));
        r.setPreparationTime(10);
        r.setCategory("Breakfast");
        r.setFoodType(FoodType.VEGETARIAN);
        r.setAvailable(true);
        r.setCreatedAt(Instant.now());
        return r;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateMenuItem() throws Exception {

        CreateMenuItemRequest request = new CreateMenuItemRequest();
        request.setName("Masala Dosa");
        request.setDescription("Crispy dosa");
        request.setPrice(new BigDecimal("60.00"));
        request.setPreparationTime(10);
        request.setCategory("Breakfast");
        request.setFoodType(FoodType.VEGETARIAN);

        Mockito.when(service.create(any())).thenReturn(mockResponse());

        mockMvc.perform(post("/api/menu-items")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetMenuItem() throws Exception {

        Mockito.when(service.get(ITEM_ID)).thenReturn(mockResponse());

        mockMvc.perform(get("/api/menu-items/{id}", ITEM_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ITEM_ID.toString()));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldListMenuItems() throws Exception {

        Mockito.when(service.getAll()).thenReturn(List.of(mockResponse()));

        mockMvc.perform(get("/api/menu-items"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateMenuItem() throws Exception {

        UpdateMenuItemRequest request = new UpdateMenuItemRequest();
        request.setName("Paneer Dosa");
        request.setDescription("Crispy Paneer dosa");
        request.setPrice(new BigDecimal("90.00"));
        request.setPreparationTime(10);
        request.setCategory("Breakfast");
        request.setFoodType(FoodType.VEGETARIAN);


        MenuItemResponse updated = mockResponse();
        updated.setName("Paneer Dosa");

        Mockito.when(service.update(eq(ITEM_ID), any())).thenReturn(updated);

        mockMvc.perform(put("/api/menu-items/{id}", ITEM_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateAvailability() throws Exception {

        UpdateAvailabilityRequest req = new UpdateAvailabilityRequest();
        req.setAvailable(false);

        MenuItemResponse resp = mockResponse();
        resp.setAvailable(false);

        Mockito.when(service.updateAvailability(ITEM_ID, false)).thenReturn(resp);

        mockMvc.perform(patch("/api/menu-items/{id}/availability", ITEM_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldReturnForbiddenWithWrongRole() throws Exception {
        mockMvc.perform(get("/api/menu-items/{id}", ITEM_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedWithoutLogin() throws Exception {
        mockMvc.perform(get("/api/menu-items/{id}", ITEM_ID))
                .andExpect(status().isUnauthorized());
    }
}
