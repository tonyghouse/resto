package com.tonyghouse.auth_utility_service.config;

import com.tonyghouse.auth_utility_service.model.Client;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "security.clients")
@Data
public class ClientConfig {

    private Client admin;
    private Client staff;
    private Client service;



    public Client resolve(String clientId) {
        if (admin != null && admin.getId().equals(clientId)) return admin;
        if (staff != null && staff.getId().equals(clientId)) return staff;
        if (service != null && service.getId().equals(clientId)) return service;
        return null;
    }
}
