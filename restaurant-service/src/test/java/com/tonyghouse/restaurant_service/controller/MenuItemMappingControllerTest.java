package com.tonyghouse.restaurant_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tonyghouse.restaurant_service.dto.AddItemToMenuRequest;
import com.tonyghouse.restaurant_service.dto.MenuItemSummaryResponse;
import com.tonyghouse.restaurant_service.service.MenuItemMappingService;

import java.time.Clock;
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

@WebMvcTest(MenuItemMappingController.class)
@Import(MenuItemMappingControllerTest.TestSecurityConfig.class)
class MenuItemMappingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MenuItemMappingService service;

    private final UUID MENU_ID = UUID.randomUUID();
    private final UUID ITEM_ID = UUID.randomUUID();

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
    void shouldAddItemToMenu() throws Exception {

        AddItemToMenuRequest request = new AddItemToMenuRequest();
        request.setItemId(ITEM_ID);

        mockMvc.perform(post("/api/menus/{menuId}/items", MENU_ID)
                        .with(csrf())   // required
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Mockito.verify(service)
                .addItemToMenu(MENU_ID, ITEM_ID);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRemoveItemFromMenu() throws Exception {

        mockMvc.perform(delete("/api/menus/{menuId}/items/{itemId}", MENU_ID, ITEM_ID)
                        .with(csrf()))   // required
                .andExpect(status().isOk());

        Mockito.verify(service)
                .removeItemFromMenu(MENU_ID, ITEM_ID);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldListMenuItems() throws Exception {

        MenuItemSummaryResponse resp = new MenuItemSummaryResponse();
        resp.setId(ITEM_ID);
        resp.setName("Masala Dosa");

        Mockito.when(service.listItems(MENU_ID))
                .thenReturn(List.of(resp));

        mockMvc.perform(get("/api/menus/{menuId}/items", MENU_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Masala Dosa"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenWithWrongRole() throws Exception {
        mockMvc.perform(get("/api/menus/{menuId}/items", MENU_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedWithoutLogin() throws Exception {
        mockMvc.perform(get("/api/menus/{menuId}/items", MENU_ID))
                .andExpect(status().isUnauthorized());
    }
}
