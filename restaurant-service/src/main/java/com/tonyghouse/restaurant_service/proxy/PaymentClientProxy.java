package com.tonyghouse.restaurant_service.proxy;

import com.tonyghouse.restaurant_service.dto.auth.TokenRequest;
import com.tonyghouse.restaurant_service.dto.auth.TokenResponse;
import com.tonyghouse.restaurant_service.dto.payment.CreatePaymentRequest;
import com.tonyghouse.restaurant_service.dto.payment.CreatePaymentResponse;
import com.tonyghouse.restaurant_service.dto.payment.PaymentResponse;
import com.tonyghouse.restaurant_service.dto.payment.RefundRequest;
import com.tonyghouse.restaurant_service.dto.payment.RefundResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;
import java.util.function.Supplier;

@Component
public class PaymentClientProxy {

    private static final int MAX_RETRIES = 5;
    private static final long RETRY_DELAY_MS = 2000;

    private final RestTemplate restTemplate;

    @Value("${payment_service.url}")
    private String paymentServiceUrl;

    @Value("${auth_service.url}")
    private String authServiceUrl;

    @Value("${restaurant_service.client_id}")
    private String clientId;

    @Value("${restaurant_service.client_secret}")
    private String clientSecret;

    public PaymentClientProxy(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    public CreatePaymentResponse createPayment(
            String idempotencyKey,
            CreatePaymentRequest request
    ) {

        return executeWithRetry(() -> {
            HttpHeaders headers = authHeaders();
            headers.set("Idempotency-Key", idempotencyKey);

            HttpEntity<CreatePaymentRequest> entity =
                    new HttpEntity<>(request, headers);

            return restTemplate.postForObject(
                    paymentServiceUrl + "/api/payments",
                    entity,
                    CreatePaymentResponse.class
            );
        });
    }

    public PaymentResponse processPayment(UUID paymentId) {

        return executeWithRetry(() -> {
            HttpEntity<Void> entity =
                    new HttpEntity<>(authHeaders());

            return restTemplate.postForObject(
                    paymentServiceUrl + "/api/payments/" + paymentId + "/process",
                    entity,
                    PaymentResponse.class
            );
        });
    }

    public PaymentResponse getPayment(UUID paymentId) {

        return executeWithRetry(() -> {
            HttpEntity<Void> entity =
                    new HttpEntity<>(authHeaders());

            return restTemplate.exchange(
                    paymentServiceUrl + "/api/payments/" + paymentId,
                    HttpMethod.GET,
                    entity,
                    PaymentResponse.class
            ).getBody();
        });
    }

    public RefundResponse refund(UUID paymentId, RefundRequest request) {

        return executeWithRetry(() -> {
            HttpHeaders headers = authHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<RefundRequest> entity =
                    new HttpEntity<>(request, headers);

            return restTemplate.postForObject(
                    paymentServiceUrl + "/api/payments/" + paymentId + "/refund",
                    entity,
                    RefundResponse.class
            );
        });
    }

    private <T> T executeWithRetry(Supplier<T> action) {

        int attempt = 0;
        RuntimeException lastException = null;

        while (attempt < MAX_RETRIES) {
            try {
                return action.get();
            } catch (RestClientException ex) {
                lastException = ex;
                attempt++;

                if (attempt >= MAX_RETRIES) {
                    break;
                }

                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }
            }
        }

        throw new RuntimeException(
                "Payment service call failed after " + MAX_RETRIES + " retries",
                lastException
        );
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        return headers;
    }

    private String getAccessToken() {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        TokenRequest request = new TokenRequest();
        request.setClient_id(clientId);
        request.setClient_secret(clientSecret);

        HttpEntity<TokenRequest> entity =
                new HttpEntity<>(request, headers);

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
