package com.tonyghouse.restaurant_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tonyghouse.restaurant_service.constants.OrderStatus;
import com.tonyghouse.restaurant_service.dto.*;
import com.tonyghouse.restaurant_service.service.OrderPaymentService;
import com.tonyghouse.restaurant_service.service.OrderService;
import com.tonyghouse.restaurant_service.service.OrderStateService;

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

@WebMvcTest(OrderController.class)
@Import(OrderControllerTest.TestSecurityConfig.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @MockBean
    private OrderStateService orderStateService;

    @MockBean
    private OrderPaymentService orderPaymentService;

    private final UUID ORDER_ID = UUID.randomUUID();

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
    void shouldPreviewPrice() throws Exception {

        PricePreviewResponse resp = new PricePreviewResponse();
        Mockito.when(orderService.preview(any())).thenReturn(resp);

        mockMvc.perform(post("/api/orders/price-preview")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }


    @Test
    @WithMockUser(roles = "STAFF")
    void shouldCreateOrder() throws Exception {

        OrderResponse resp = new OrderResponse();
        resp.setOrderId(ORDER_ID);

        Mockito.when(orderService.create(any())).thenReturn(resp);

        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(ORDER_ID.toString()));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldGetOrder() throws Exception {

        OrderResponse resp = new OrderResponse();
        resp.setOrderId(ORDER_ID);

        Mockito.when(orderService.get(ORDER_ID)).thenReturn(resp);

        mockMvc.perform(get("/api/orders/{id}", ORDER_ID))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void shouldAcceptOrder() throws Exception {
        mockMvc.perform(post("/api/orders/{id}/accept", ORDER_ID).with(csrf()))
                .andExpect(status().isOk());

        Mockito.verify(orderStateService).accept(ORDER_ID);
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void shouldMarkPreparing() throws Exception {
        mockMvc.perform(post("/api/orders/{id}/preparing", ORDER_ID).with(csrf()))
                .andExpect(status().isOk());

        Mockito.verify(orderStateService).markPreparing(ORDER_ID);
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void shouldMarkReady() throws Exception {
        mockMvc.perform(post("/api/orders/{id}/ready", ORDER_ID).with(csrf()))
                .andExpect(status().isOk());

        Mockito.verify(orderStateService).markReady(ORDER_ID);
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void shouldDeliver() throws Exception {
        mockMvc.perform(post("/api/orders/{id}/delivered", ORDER_ID).with(csrf()))
                .andExpect(status().isOk());

        Mockito.verify(orderStateService).markDelivered(ORDER_ID);
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void shouldCancel() throws Exception {
        mockMvc.perform(post("/api/orders/{id}/cancel", ORDER_ID).with(csrf()))
                .andExpect(status().isOk());

        Mockito.verify(orderStateService).cancel(ORDER_ID);
    }


    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldGetHistory() throws Exception {

        OrderStatusHistoryResponse h =
                new OrderStatusHistoryResponse(OrderStatus.ACCEPTED, OrderStatus.CREATED, Instant.now());
        Mockito.when(orderStateService.history(ORDER_ID)).thenReturn(List.of(h));

        mockMvc.perform(get("/api/orders/{id}/status-history", ORDER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldInitiatePayment() throws Exception {
        mockMvc.perform(post("/api/orders/{id}/payments", ORDER_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        Mockito.verify(orderPaymentService).initiatePayment(eq(ORDER_ID), any());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldHandleCallback() throws Exception {
        mockMvc.perform(post("/api/orders/{id}/payments/callback", ORDER_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldRetryPayment() throws Exception {
        mockMvc.perform(post("/api/orders/{id}/payments/retry", ORDER_ID)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldRefund() throws Exception {
        mockMvc.perform(post("/api/orders/{id}/refunds", ORDER_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnForbiddenForAdmin() throws Exception {
        mockMvc.perform(get("/api/orders/{id}", ORDER_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedWithoutLogin() throws Exception {
        mockMvc.perform(get("/api/orders/{id}", ORDER_ID))
                .andExpect(status().isUnauthorized());
    }
}
