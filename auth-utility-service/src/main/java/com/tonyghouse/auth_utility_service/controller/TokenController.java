package com.tonyghouse.auth_utility_service.controller;


import com.tonyghouse.auth_utility_service.model.TokenRequest;
import com.tonyghouse.auth_utility_service.model.TokenResponse;
import com.tonyghouse.auth_utility_service.service.TokenService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/token")
public class TokenController {

    private final TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping
    public TokenResponse issueToken(@RequestBody TokenRequest request) {
        String token = tokenService.generateToken(request);
        return new TokenResponse(token, "Bearer", 1800);
    }
}
