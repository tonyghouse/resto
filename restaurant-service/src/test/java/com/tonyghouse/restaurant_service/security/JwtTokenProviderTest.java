package com.tonyghouse.restaurant_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtTokenProviderTest {

    private JwtTokenProvider provider;

    private final String secret =
            "my-super-secret-key-my-super-ghouse-key-12345"; // >= 32 chars for HS256
    private final String issuer = "payment-service";

    private Key key;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider();

        // inject @Value fields manually
        ReflectionTestUtils.setField(provider, "secret", secret);
        ReflectionTestUtils.setField(provider, "issuer", issuer);

        key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void shouldValidateAndReturnClaimsForValidToken() {
        String token = createToken("user1", List.of("CUSTOMER"));

        Claims claims = provider.validateAndGetClaims(token);

        assertEquals("user1", claims.getSubject());
    }

    @Test
    void shouldThrowWhenIssuerIsInvalid() {
        String token = Jwts.builder()
                .setSubject("user1")
                .setIssuer("wrong-issuer")
                .signWith(key)
                .compact();

        assertThrows(Exception.class,
                () -> provider.validateAndGetClaims(token));
    }

    @Test
    void shouldThrowWhenSignatureIsInvalid() {
        Key otherKey = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);

        String token = Jwts.builder()
                .setSubject("user1")
                .setIssuer(issuer)
                .signWith(otherKey)
                .compact();

        assertThrows(Exception.class,
                () -> provider.validateAndGetClaims(token));
    }

    @Test
    void shouldExtractRolesFromClaims() {
        String token = createToken("user1", List.of("ADMIN", "CUSTOMER"));

        Claims claims = provider.validateAndGetClaims(token);

        List<String> roles = provider.getRoles(claims);

        assertEquals(List.of("ADMIN", "CUSTOMER"), roles);
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
