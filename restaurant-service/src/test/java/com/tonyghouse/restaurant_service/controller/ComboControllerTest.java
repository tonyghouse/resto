package com.tonyghouse.restaurant_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tonyghouse.restaurant_service.dto.*;
import com.tonyghouse.restaurant_service.service.ComboService;

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

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ComboController.class)
@Import(ComboControllerTest.TestSecurityConfig.class)
class ComboControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ComboService comboService;

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
    void shouldCreateCombo() throws Exception {

        CreateComboRequest request = new CreateComboRequest();
        request.setName("Weekend Combo");
        request.setComboPrice(BigDecimal.valueOf(100.00));

        ComboResponse response = new ComboResponse();
        response.setId(UUID.randomUUID());

        Mockito.when(comboService.create(any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/combos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetCombo() throws Exception {

        UUID comboId = UUID.randomUUID();

        ComboResponse response = new ComboResponse();
        response.setId(comboId);

        Mockito.when(comboService.get(comboId))
                .thenReturn(response);

        mockMvc.perform(get("/api/combos/{comboId}", comboId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldListCombos() throws Exception {

        List<ComboResponse> content = Arrays.asList(
                new ComboResponse(),
                new ComboResponse()
        );

        Page<ComboResponse> page =
                new PageImpl<>(content, PageRequest.of(0, 20), content.size());

        Mockito.when(comboService.getAll(Mockito.any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/combos")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateCombo() throws Exception {

        UUID comboId = UUID.randomUUID();

        UpdateComboRequest request = new UpdateComboRequest();
        request.setName("Updated Combo");
        request.setComboPrice(BigDecimal.valueOf(100.00));

        ComboResponse response = new ComboResponse();
        response.setId(comboId);

        Mockito.when(comboService.update(eq(comboId), any()))
                .thenReturn(response);

        mockMvc.perform(put("/api/combos/{comboId}", comboId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateComboStatus() throws Exception {

        UUID comboId = UUID.randomUUID();

        UpdateComboStatusRequest request = new UpdateComboStatusRequest();
        request.setActive(true);

        ComboResponse response = new ComboResponse();
        response.setId(comboId);

        Mockito.when(comboService.updateStatus(comboId, true))
                .thenReturn(response);

        mockMvc.perform(patch("/api/combos/{comboId}/status", comboId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAddItemToCombo() throws Exception {

        UUID comboId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        mockMvc.perform(post("/api/combos/{comboId}/items", comboId)
                        .with(csrf())
                        .param("itemId", itemId.toString()))
                .andExpect(status().isOk());

        Mockito.verify(comboService).addItem(comboId, itemId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRemoveItemFromCombo() throws Exception {

        UUID comboId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        mockMvc.perform(delete("/api/combos/{comboId}/items/{itemId}", comboId, itemId)
                        .with(csrf()))
                .andExpect(status().isOk());

        Mockito.verify(comboService).removeItem(comboId, itemId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenWithWrongRole() throws Exception {

        UUID comboId = UUID.randomUUID();

        mockMvc.perform(get("/api/combos/{comboId}", comboId))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedWithoutLogin() throws Exception {

        UUID comboId = UUID.randomUUID();

        mockMvc.perform(get("/api/combos/{comboId}", comboId))
                .andExpect(status().isUnauthorized());
    }
}
