package com.tonyghouse.restaurant_service.dto.auth;

import lombok.Data;

@Data
public class TokenRequest {
    private String client_id;
    private String client_secret;
}
