package com.tonyghouse.payment_service.security;

import com.tonyghouse.payment_service.controller.PaymentController;
import com.tonyghouse.payment_service.entity.Payment;
import com.tonyghouse.payment_service.service.PaymentService;
import com.tonyghouse.payment_service.service.RefundService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PaymentController.class)
@Import({SecurityConfig.class, JwtTokenProvider.class})
@TestPropertySource(properties = {
        "security.jwt.secret=my-super-secret-key-my-super-secret-key-12345",
        "security.jwt.issuer=payment-service"
})
class SecurityConfigTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    PaymentService paymentService;

    @MockBean
    RefundService refundService;

    private Key key;
    private final String issuer = "payment-service";

    @BeforeEach
    void setup() {
        key = Keys.hmacShaKeyFor(
                "my-super-secret-key-my-super-secret-key-12345"
                        .getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void securedEndpointShouldReturn403WithoutToken() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(get("/api/payments/" + id))
                .andExpect(status().isForbidden());
    }
    @Test
    void validTokenShouldAllowAccess() throws Exception {
        UUID id = UUID.randomUUID();

        Payment payment = new Payment();
        payment.setPaymentId(id);

        when(paymentService.getPayment(id)).thenReturn(payment);

        String token = createToken("svc", List.of("RESTAURANT_SERVICE"));

        mockMvc.perform(get("/api/payments/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    private String createToken(String subject, List<String> roles) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuer(issuer)
                .claim("roles", roles)
                .signWith(key)
                .compact();
    }
}
