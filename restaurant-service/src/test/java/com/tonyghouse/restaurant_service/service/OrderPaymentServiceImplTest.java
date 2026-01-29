package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.constants.OrderStatus;
import com.tonyghouse.restaurant_service.constants.payment.PaymentStatus;
import com.tonyghouse.restaurant_service.dto.InitiatePaymentRequest;
import com.tonyghouse.restaurant_service.dto.PaymentCallbackRequest;
import com.tonyghouse.restaurant_service.dto.PriceBreakdown;
import com.tonyghouse.restaurant_service.dto.RefundRequestDto;
import com.tonyghouse.restaurant_service.dto.payment.PaymentResponse;
import com.tonyghouse.restaurant_service.entity.Order;
import com.tonyghouse.restaurant_service.proxy.PaymentClientProxy;
import com.tonyghouse.restaurant_service.repo.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderPaymentServiceImplTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    OrderPricingService pricingService;

    @Mock
    PaymentClientProxy paymentClientProxy;

    @InjectMocks
    OrderPaymentServiceImpl service;

    @Test
    void initiatePayment_success() {
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.ACCEPTED);

        PriceBreakdown breakdown = new PriceBreakdown();
        breakdown.setGrandTotal(BigDecimal.valueOf(100));
        breakdown.setTax(BigDecimal.valueOf(10));

        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(paymentId);

        InitiatePaymentRequest req = new InitiatePaymentRequest();

        Mockito.when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        Mockito.when(pricingService.recalculateFromOrder(order)).thenReturn(breakdown);
        Mockito.when(paymentClientProxy.createPayment(Mockito.any(), Mockito.any()))
                .thenReturn(response);

        service.initiatePayment(orderId, req);

        assertEquals(paymentId, order.getPaymentId());
        Mockito.verify(paymentClientProxy).processPayment(paymentId);
        Mockito.verify(orderRepository).save(order);
    }

    @Test
    void initiatePayment_wrongStatus() {
        UUID orderId = UUID.randomUUID();

        Order order = new Order();
        order.setStatus(OrderStatus.CREATED);

        Mockito.when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class,
                () -> service.initiatePayment(orderId, new InitiatePaymentRequest()));
    }

    @Test
    void initiatePayment_alreadyInitiated() {
        UUID orderId = UUID.randomUUID();

        Order order = new Order();
        order.setStatus(OrderStatus.ACCEPTED);
        order.setPaymentId(UUID.randomUUID());

        Mockito.when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class,
                () -> service.initiatePayment(orderId, new InitiatePaymentRequest()));
    }

    @Test
    void handleCallback_success() {
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        Order order = new Order();
        order.setPaymentId(paymentId);

        PaymentCallbackRequest req = new PaymentCallbackRequest();
        req.setPaymentId(paymentId);
        req.setStatus(PaymentStatus.SUCCESS);

        Mockito.when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        service.handleCallback(orderId, req);
    }

    @Test
    void handleCallback_paymentMismatch() {
        UUID orderId = UUID.randomUUID();

        Order order = new Order();
        order.setPaymentId(UUID.randomUUID());

        PaymentCallbackRequest req = new PaymentCallbackRequest();
        req.setPaymentId(UUID.randomUUID());

        Mockito.when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class,
                () -> service.handleCallback(orderId, req));
    }

    @Test
    void retryPayment_success() {
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        Order order = new Order();
        order.setPaymentId(paymentId);

        Mockito.when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        service.retryPayment(orderId);

        Mockito.verify(paymentClientProxy).processPayment(paymentId);
    }

    @Test
    void refund_success() {
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        Order order = new Order();
        order.setPaymentId(paymentId);

        RefundRequestDto req = new RefundRequestDto();
        req.setAmount(BigDecimal.valueOf(50));
        req.setReason("TEST");

        Mockito.when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        service.refund(orderId, req);

        Mockito.verify(paymentClientProxy)
                .refund(Mockito.eq(paymentId), Mockito.any());
    }
}
