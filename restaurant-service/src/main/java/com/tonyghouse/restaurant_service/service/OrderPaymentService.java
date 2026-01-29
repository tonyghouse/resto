package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.dto.InitiatePaymentRequest;
import com.tonyghouse.restaurant_service.dto.PaymentCallbackRequest;
import com.tonyghouse.restaurant_service.dto.RefundRequestDto;

import java.util.UUID;

public interface OrderPaymentService {

    void initiatePayment(UUID orderId, InitiatePaymentRequest request);

    void handleCallback(UUID orderId, PaymentCallbackRequest request);

    void retryPayment(UUID orderId);

    void refund(UUID orderId, RefundRequestDto request);
}
