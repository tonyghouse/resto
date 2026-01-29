package com.tonyghouse.restaurant_service.proxy;

import com.tonyghouse.restaurant_service.dto.payment.CreatePaymentRequest;
import com.tonyghouse.restaurant_service.dto.payment.PaymentResponse;
import com.tonyghouse.restaurant_service.dto.payment.RefundRequest;
import com.tonyghouse.restaurant_service.dto.payment.RefundResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class PaymentClient {

    private final RestTemplate restTemplate;
    private static final String BASE_URL = "http://localhost:8081/api/payments";

    public PaymentClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public PaymentResponse createPayment(
            String idempotencyKey,
            CreatePaymentRequest request) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Idempotency-Key", idempotencyKey);

        HttpEntity<CreatePaymentRequest> entity =
                new HttpEntity<>(request, headers);

        return restTemplate.postForObject(
                BASE_URL,
                entity,
                PaymentResponse.class
        );
    }

    public PaymentResponse processPayment(UUID paymentId) {
        return restTemplate.postForObject(
                BASE_URL + "/" + paymentId + "/process",
                null,
                PaymentResponse.class
        );
    }

    public RefundResponse refund(UUID paymentId, RefundRequest request) {
        return restTemplate.postForObject(
                BASE_URL + "/" + paymentId + "/refund",
                request,
                RefundResponse.class
        );
    }
}
