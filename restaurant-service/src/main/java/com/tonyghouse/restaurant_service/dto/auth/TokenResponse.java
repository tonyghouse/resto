package com.tonyghouse.restaurant_service.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenResponse {
    private String access_token;
    private String token_type;
    private long expires_in;
}
