package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.proxy.PaymentClientProxy;
import com.tonyghouse.restaurant_service.constants.OrderStatus;
import com.tonyghouse.restaurant_service.constants.payment.PaymentStatus;
import com.tonyghouse.restaurant_service.dto.InitiatePaymentRequest;
import com.tonyghouse.restaurant_service.dto.PaymentCallbackRequest;
import com.tonyghouse.restaurant_service.dto.PriceBreakdown;
import com.tonyghouse.restaurant_service.dto.RefundRequestDto;
import com.tonyghouse.restaurant_service.dto.payment.CreatePaymentRequest;
import com.tonyghouse.restaurant_service.dto.payment.PaymentResponse;
import com.tonyghouse.restaurant_service.dto.payment.RefundRequest;
import com.tonyghouse.restaurant_service.entity.Order;
import com.tonyghouse.restaurant_service.repo.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class OrderPaymentServiceImpl implements OrderPaymentService {

    private final OrderRepository orderRepository;
    private final OrderPricingService pricingService;
    private final PaymentClientProxy paymentClientProxy;

    public OrderPaymentServiceImpl(
            OrderRepository orderRepository,
            OrderPricingService pricingService,
            PaymentClientProxy paymentClientProxy) {
        this.orderRepository = orderRepository;
        this.pricingService = pricingService;
        this.paymentClientProxy = paymentClientProxy;
    }

    @Override
    public void initiatePayment(UUID orderId, InitiatePaymentRequest req) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (order.getPaymentId() != null) {
            throw new IllegalStateException("Payment already initiated");
        }

        if (order.getStatus() != OrderStatus.ACCEPTED) {
            throw new IllegalStateException("Payment allowed only after ACCEPTED");
        }

        PriceBreakdown breakdown =
                pricingService.recalculateFromOrder(order);

        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setOrderId(order.getId());
        request.setMethod(req.getMethod());
        request.setCurrency(req.getCurrency());
        request.setTotalAmount(breakdown.getGrandTotal());
        request.setTaxAmount(breakdown.getTax());
        request.setPayableAmount(breakdown.getGrandTotal());

        String idempotencyKey =
                "order-" + orderId;

        PaymentResponse response =
                paymentClientProxy.createPayment(idempotencyKey, request);

        order.setPaymentId(response.getPaymentId());
        orderRepository.save(order);

        // async or sync – your call
        paymentClientProxy.processPayment(response.getPaymentId());
    }

    @Override
    public void handleCallback(UUID orderId, PaymentCallbackRequest request) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getPaymentId().equals(request.getPaymentId())) {
            throw new IllegalArgumentException("Payment mismatch");
        }

        if (request.getStatus() == PaymentStatus.SUCCESS) {
            // order stays ACCEPTED → PREPARING happens manually
            return;
        }

        if (request.getStatus() == PaymentStatus.FAILED) {
            // allow retry
            return;
        }
    }

    @Override
    public void retryPayment(UUID orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (order.getPaymentId() == null) {
            throw new IllegalStateException("No payment to retry");
        }

        paymentClientProxy.processPayment(order.getPaymentId());
    }

    @Override
    public void refund(UUID orderId, RefundRequestDto req) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (order.getPaymentId() == null) {
            throw new IllegalStateException("Payment not initiated");
        }

        RefundRequest refund = new RefundRequest();
        refund.setAmount(req.getAmount());
        refund.setReason(req.getReason());

        paymentClientProxy.refund(order.getPaymentId(), refund);
    }
}
