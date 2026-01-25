package com.tonyghouse.auth_utility_service.model;

import lombok.Data;

@Data
    public class Client {
        private String id;
        private String secret;
        private String roles;
    }