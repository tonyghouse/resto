package com.tonyghouse.auth_utility_service.service;

import com.tonyghouse.auth_utility_service.config.ClientConfig;
import com.tonyghouse.auth_utility_service.model.TokenRequest;
import com.tonyghouse.auth_utility_service.util.JwtUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TokenService {

    private final ClientConfig clientConfig;
    private final JwtUtil jwtUtil;

    public TokenService(ClientConfig clientConfig, JwtUtil jwtUtil) {
        this.clientConfig = clientConfig;
        this.jwtUtil = jwtUtil;
    }

    public String generateToken(TokenRequest request) {
        var client = clientConfig.resolve(request.getClient_id());

        if (client == null || !client.getSecret().equals(request.getClient_secret())) {
            throw new RuntimeException("Invalid client credentials");
        }

        return jwtUtil.generateToken(
                client.getId(),
                List.of(client.getRoles())
        );
    }
}
