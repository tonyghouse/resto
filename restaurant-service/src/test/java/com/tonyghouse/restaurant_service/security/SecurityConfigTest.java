package com.tonyghouse.restaurant_service.security;

import com.tonyghouse.restaurant_service.controller.BranchController;
import com.tonyghouse.restaurant_service.dto.BranchResponse;
import com.tonyghouse.restaurant_service.service.BranchService;
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
import java.time.Clock;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BranchController.class)
@Import({SecurityConfig.class, JwtTokenProvider.class})
@TestPropertySource(properties = {
        "security.jwt.secret=my-super-secret-key-my-super-ghouse-key-12345",
        "security.jwt.issuer=payment-service"
})
class SecurityConfigTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    BranchService branchService;

    @MockBean
    Clock clock;

    private Key key;
    private final String issuer = "payment-service";

    @BeforeEach
    void setup() {
        key = Keys.hmacShaKeyFor(
                "my-super-secret-key-my-super-ghouse-key-12345"
                        .getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void securedEndpointShouldReturn403WithoutToken() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(get("/api/branches/" + id))
                .andExpect(status().isForbidden());
    }

    @Test
    void validAdminTokenShouldAllowAccess() throws Exception {
        UUID id = UUID.randomUUID();

        BranchResponse response = new BranchResponse();
        response.setId(id);

        when(branchService.getBranch(id)).thenReturn(response);

        String token = createToken("svc", List.of("ADMIN"));

        mockMvc.perform(get("/api/branches/" + id)
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
