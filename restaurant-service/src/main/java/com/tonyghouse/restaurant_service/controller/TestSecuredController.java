package com.tonyghouse.restaurant_service.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestSecuredController {

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminEndpoint() {
        return "Hello ADMIN  – you have full access";
    }

    @GetMapping("/staff")
    @PreAuthorize("hasRole('STAFF')")
    public String staffEndpoint() {
        return "Hello STAFF  – operational access granted";
    }

    @GetMapping("/internal/service")
    @PreAuthorize("hasRole('RESTAURANT_SERVICE')")
    public String serviceEndpoint() {
        return "Hello SERVICE – internal access granted";
    }
}
