package com.tonyghouse.restaurant_service.proxy;

import com.tonyghouse.restaurant_service.dto.auth.TokenRequest;
import com.tonyghouse.restaurant_service.dto.auth.TokenResponse;
import com.tonyghouse.restaurant_service.dto.payment.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PaymentClientProxyTest {

    private RestTemplate restTemplate;
    private PaymentClientProxy proxy;

    @BeforeEach
    void setup() {
        restTemplate = mock(RestTemplate.class);
        proxy = new PaymentClientProxy(restTemplate);

        ReflectionTestUtils.setField(proxy, "paymentServiceUrl", "http://payment");
        ReflectionTestUtils.setField(proxy, "authServiceUrl", "http://auth");
        ReflectionTestUtils.setField(proxy, "clientId", "id");
        ReflectionTestUtils.setField(proxy, "clientSecret", "secret");
    }

    private void mockTokenSuccess() {
        TokenResponse token = new TokenResponse();
        token.setAccess_token("token-123");

        when(restTemplate.postForObject(
                eq("http://auth/token"),
                any(HttpEntity.class),
                eq(TokenResponse.class)
        )).thenReturn(token);
    }

    @Test
    void createPayment_success() {
        mockTokenSuccess();

        CreatePaymentResponse expected = new CreatePaymentResponse();

        when(restTemplate.postForObject(
                eq("http://payment/api/payments"),
                any(HttpEntity.class),
                eq(CreatePaymentResponse.class)
        )).thenReturn(expected);

        CreatePaymentResponse result =
                proxy.createPayment("idem-1", new CreatePaymentRequest());

        assertThat(result).isSameAs(expected);

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(
                eq("http://payment/api/payments"),
                captor.capture(),
                eq(CreatePaymentResponse.class)
        );

        HttpHeaders headers = captor.getValue().getHeaders();
        assertThat(headers.getFirst("Idempotency-Key")).isEqualTo("idem-1");
        assertThat(headers.getFirst(HttpHeaders.AUTHORIZATION)).startsWith("Bearer");
    }

    @Test
    void processPayment_success() {
        mockTokenSuccess();

        PaymentResponse response = new PaymentResponse();

        when(restTemplate.postForObject(
                contains("/process"),
                any(HttpEntity.class),
                eq(PaymentResponse.class)
        )).thenReturn(response);

        assertThat(proxy.processPayment(UUID.randomUUID()))
                .isSameAs(response);
    }

    @Test
    void getPayment_success() {
        mockTokenSuccess();

        PaymentResponse response = new PaymentResponse();

        ResponseEntity<PaymentResponse> entity =
                new ResponseEntity<>(response, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(PaymentResponse.class)
        )).thenReturn(entity);

        assertThat(proxy.getPayment(UUID.randomUUID()))
                .isSameAs(response);
    }

    @Test
    void refund_setsJsonHeader() {
        mockTokenSuccess();

        RefundResponse response = new RefundResponse();

        when(restTemplate.postForObject(
                contains("/refund"),
                any(HttpEntity.class),
                eq(RefundResponse.class)
        )).thenReturn(response);

        proxy.refund(UUID.randomUUID(), new RefundRequest());

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate).postForObject(
                contains("/refund"),
                captor.capture(),
                eq(RefundResponse.class)
        );

        assertThat(captor.getValue().getHeaders().getContentType())
                .isEqualTo(MediaType.APPLICATION_JSON);
    }

    @Test
    void shouldRetryAndThenSucceed() {
        mockTokenSuccess();

        when(restTemplate.postForObject(
                anyString(),
                any(),
                eq(CreatePaymentResponse.class)
        ))
                .thenThrow(new RestClientException("fail"))
                .thenReturn(new CreatePaymentResponse());

        CreatePaymentResponse res =
                proxy.createPayment("k", new CreatePaymentRequest());

        assertThat(res).isNotNull();

        verify(restTemplate, times(2)).postForObject(
                anyString(), any(), eq(CreatePaymentResponse.class));
    }

    @Test
    void shouldFailAfterMaxRetries() {
        mockTokenSuccess();

        when(restTemplate.postForObject(
                anyString(),
                any(),
                eq(CreatePaymentResponse.class)
        )).thenThrow(new RestClientException("fail"));

        assertThatThrownBy(() ->
                proxy.createPayment("k", new CreatePaymentRequest())
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("failed after");
    }

    @Test
    void interruptedDuringRetry_shouldThrowRuntime() {
        mockTokenSuccess();

        Thread.currentThread().interrupt();

        when(restTemplate.postForObject(
                anyString(),
                any(),
                eq(CreatePaymentResponse.class)
        )).thenThrow(new RestClientException("fail"));

        assertThatThrownBy(() ->
                proxy.createPayment("k", new CreatePaymentRequest())
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Retry interrupted");

        Thread.interrupted(); // clear flag
    }


    @Test
    void tokenResponseNull_shouldThrow() {
        when(restTemplate.postForObject(
                eq("http://auth/token"),
                any(),
                eq(TokenResponse.class)
        )).thenReturn(null);

        assertThatThrownBy(() ->
                proxy.processPayment(UUID.randomUUID())
        ).isInstanceOf(RuntimeException.class);
    }

    @Test
    void tokenAccessTokenNull_shouldThrow() {
        TokenResponse token = new TokenResponse();

        when(restTemplate.postForObject(
                eq("http://auth/token"),
                any(),
                eq(TokenResponse.class)
        )).thenReturn(token);

        assertThatThrownBy(() ->
                proxy.processPayment(UUID.randomUUID())
        ).isInstanceOf(RuntimeException.class);
    }
}
