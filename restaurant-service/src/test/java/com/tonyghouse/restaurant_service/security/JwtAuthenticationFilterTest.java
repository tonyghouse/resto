package com.tonyghouse.restaurant_service.security;

import com.tonyghouse.restaurant_service.constants.SecurityConstants;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    JwtTokenProvider tokenProvider;

    @Mock
    FilterChain filterChain;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(tokenProvider);
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSetAuthenticationForValidToken() throws Exception {
        String token = "valid-token";
        Claims claims = mock(Claims.class);

        when(request.getHeader(SecurityConstants.AUTH_HEADER))
                .thenReturn("Bearer " + token);
        when(tokenProvider.validateAndGetClaims(token)).thenReturn(claims);
        when(claims.getSubject()).thenReturn("user1");
        when(tokenProvider.getRoles(claims)).thenReturn(List.of("USER"));

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("user1", auth.getPrincipal());
    }

    @Test
    void shouldClearContextWhenTokenIsInvalid() throws Exception {
        when(request.getHeader(SecurityConstants.AUTH_HEADER))
                .thenReturn("Bearer invalid");

        when(tokenProvider.validateAndGetClaims("invalid"))
                .thenThrow(new RuntimeException("Invalid JWT"));

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
