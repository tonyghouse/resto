package com.tonyghouse.restaurant_service.proxy;

import com.tonyghouse.restaurant_service.dto.auth.TokenResponse;
import com.tonyghouse.restaurant_service.dto.payment.CreatePaymentRequest;
import com.tonyghouse.restaurant_service.dto.payment.PaymentResponse;
import com.tonyghouse.restaurant_service.dto.payment.RefundRequest;
import com.tonyghouse.restaurant_service.dto.payment.RefundResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class PaymentClient {

    private final RestTemplate restTemplate;

    @Value("${payment_service.url}")
    private String paymentServiceUrl;

    @Value("${auth_service.url}")
    private String authServiceUrl;

    @Value("${restaurant_service.client_id}")
    private String restaurantServiceClientId;

    @Value("${restaurant_service.client_secret}")
    private String restaurantServiceClientSecret;

    public PaymentClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public PaymentResponse createPayment(
            String idempotencyKey,
            CreatePaymentRequest request) {

        String authToken = getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.set("Idempotency-Key", idempotencyKey);

        HttpEntity<CreatePaymentRequest> entity =
                new HttpEntity<>(request, headers);

        return restTemplate.postForObject(
                paymentServiceUrl,
                entity,
                PaymentResponse.class
        );
    }

    public PaymentResponse processPayment(UUID paymentId) {
        return restTemplate.postForObject(
                paymentServiceUrl + "/" + paymentId + "/process",
                null,
                PaymentResponse.class
        );
    }

    public RefundResponse refund(UUID paymentId, RefundRequest request) {
        return restTemplate.postForObject(
                paymentServiceUrl + "/" + paymentId + "/refund",
                request,
                RefundResponse.class
        );
    }

    private String getAccessToken() {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", restaurantServiceClientId);
        body.add("client_secret", restaurantServiceClientSecret);

        HttpEntity<MultiValueMap<String, String>> entity =
                new HttpEntity<>(body, headers);

        TokenResponse response = restTemplate.postForObject(
                authServiceUrl + "/token",
                entity,
                TokenResponse.class
        );

        if (response == null || response.getAccess_token() == null) {
            throw new RuntimeException("Failed to fetch access token from auth service");
        }

        return response.getAccess_token();
    }

}
