package com.tonyghouse.auth_utility_service.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.issuer}")
    private String issuer;

    @Value("${security.jwt.expiry-seconds}")
    private long expirySeconds;

    public String generateToken(String subject, List<String> roles) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirySeconds * 1000);

        return Jwts.builder()
                .compact();
    }
}
