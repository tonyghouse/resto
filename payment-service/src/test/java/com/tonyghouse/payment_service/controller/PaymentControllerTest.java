package com.tonyghouse.payment_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tonyghouse.payment_service.constants.PaymentMethod;
import com.tonyghouse.payment_service.dto.CreatePaymentRequest;
import com.tonyghouse.payment_service.dto.RefundRequest;
import com.tonyghouse.payment_service.entity.Payment;
import com.tonyghouse.payment_service.entity.Refund;
import com.tonyghouse.payment_service.service.PaymentService;
import com.tonyghouse.payment_service.service.RefundService;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@Import(PaymentControllerTest.TestSecurityConfig.class) // enables @PreAuthorize
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private RefundService refundService;

    // Enable method security for @PreAuthorize
    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_SERVICE")
    void shouldCreatePayment() throws Exception {

        UUID paymentId = UUID.randomUUID();

        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setOrderId(UUID.randomUUID());
        request.setMethod(PaymentMethod.UPI);
        request.setCurrency("USD");
        request.setTotalAmount(BigDecimal.valueOf(100));
        request.setTaxAmount(BigDecimal.valueOf(10));
        request.setPayableAmount(BigDecimal.valueOf(110));

        Payment payment = new Payment();
        payment.setPaymentId(paymentId);

        Mockito.when(paymentService.createPayment(any(), anyString()))
                .thenReturn(payment);

        mockMvc.perform(post("/api/payments")
                        .with(csrf())
                        .header("Idempotency-Key", "abc123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }


    @Test
    @WithMockUser(roles = "RESTAURANT_SERVICE")
    void shouldProcessPayment() throws Exception {

        UUID id = UUID.randomUUID();

        Payment payment = new Payment();
        payment.setPaymentId(id);

        Mockito.when(paymentService.processPayment(id)).thenReturn(payment);

        mockMvc.perform(post("/api/payments/{id}/process", id)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_SERVICE")
    void shouldGetPayment() throws Exception {

        UUID id = UUID.randomUUID();

        Payment payment = new Payment();
        payment.setPaymentId(id);

        Mockito.when(paymentService.getPayment(id)).thenReturn(payment);

        mockMvc.perform(get("/api/payments/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_SERVICE")
    void shouldRefundPayment() throws Exception {

        UUID id = UUID.randomUUID();

        Refund refund = new Refund();
        RefundRequest request = new RefundRequest();
        request.setAmount(new BigDecimal("100"));
        request.setReason("order cancelled");

        Mockito.when(refundService.refund(eq(id), any()))
                .thenReturn(refund);

        mockMvc.perform(post("/api/payments/{id}/refund", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }


    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldReturnForbiddenWithWrongRole() throws Exception {

        UUID id = UUID.randomUUID();

        mockMvc.perform(get("/api/payments/{id}", id))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedWithoutLogin() throws Exception {

        UUID id = UUID.randomUUID();

        mockMvc.perform(get("/api/payments/{id}", id))
                .andExpect(status().isUnauthorized());
    }
}
