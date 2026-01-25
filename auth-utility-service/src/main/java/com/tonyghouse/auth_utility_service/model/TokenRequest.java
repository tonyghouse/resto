package com.tonyghouse.auth_utility_service.model;

import lombok.Data;

@Data
public class TokenRequest {
    private String client_id;
    private String client_secret;
}
