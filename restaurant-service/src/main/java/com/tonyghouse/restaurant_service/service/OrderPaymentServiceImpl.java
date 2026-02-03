package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.dto.payment.CreatePaymentResponse;
import com.tonyghouse.restaurant_service.exception.RestoRestaurantException;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@Slf4j
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
        log.info("Initiating payment. orderId={} method={} currency={}",
                orderId, req.getMethod(), req.getCurrency());

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RestoRestaurantException("Order not found", HttpStatus.NOT_FOUND));

        log.debug("Order loaded. orderId={} status={} paymentId={}",
                orderId, order.getStatus(), order.getPaymentId());

        if (order.getPaymentId() != null) {
            log.warn("Payment already initiated for orderId={} paymentId={}", orderId, order.getPaymentId());
            throw new RestoRestaurantException("Payment already initiated", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (order.getStatus() != OrderStatus.ACCEPTED) {
            log.warn("Payment not allowed. orderId={} currentStatus={}", orderId, order.getStatus());
            throw new RestoRestaurantException("Payment allowed only after ACCEPTED", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        PriceBreakdown breakdown =
                pricingService.recalculateFromOrder(order);
        log.debug("Price breakdown calculated. orderId={} total={} tax={} payable={}",
                orderId, breakdown.getGrandTotal(), breakdown.getTax(), breakdown.getGrandTotal());

        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setOrderId(order.getId());
        request.setMethod(req.getMethod());
        request.setCurrency(req.getCurrency());
        request.setTotalAmount(breakdown.getGrandTotal());
        request.setTaxAmount(breakdown.getTax());
        request.setPayableAmount(breakdown.getGrandTotal());

        String idempotencyKey =
                "order-" + orderId;

        log.info("Calling payment-service createPayment. orderId={} idempotencyKey={}", orderId, idempotencyKey);
        CreatePaymentResponse response =
                paymentClientProxy.createPayment(idempotencyKey, request);

        log.info("Payment created. orderId={} paymentId={}", orderId, response.getPaymentId());

        order.setPaymentId(response.getPaymentId());
        orderRepository.save(order);
        log.debug("PaymentId saved to order. orderId={} paymentId={}", orderId, response.getPaymentId());

        log.info("Triggering payment processing. paymentId={}", response.getPaymentId());
        paymentClientProxy.processPayment(response.getPaymentId());
    }

    @Override
    public void retryPayment(UUID orderId) {
        log.info("Retrying payment. orderId={}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RestoRestaurantException("Order not found", HttpStatus.NOT_FOUND));
        log.debug("Order loaded. paymentId={}", order.getPaymentId());

        if (order.getPaymentId() == null) {
            log.warn("Retry failed. No payment exists for orderId={}", orderId);
            throw new RestoRestaurantException("No payment to retry", HttpStatus.NOT_FOUND);
        }

        log.info("Calling payment-service processPayment. paymentId={}", order.getPaymentId());
        paymentClientProxy.processPayment(order.getPaymentId());
    }

    @Override
    public void refund(UUID orderId, RefundRequestDto req) {
        log.info("Refund requested. orderId={} amount={} reason={}",
                orderId, req.getAmount(), req.getReason());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RestoRestaurantException("Order not found", HttpStatus.NOT_FOUND));

        log.debug("Order loaded for refund. paymentId={}", order.getPaymentId());
        if (order.getPaymentId() == null) {
            log.warn("Refund rejected. No payment for orderId={}", orderId);
            throw new RestoRestaurantException("Payment not initiated", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        RefundRequest refund = new RefundRequest();
        refund.setAmount(req.getAmount());
        refund.setReason(req.getReason());

        log.info("Calling payment-service refund. paymentId={} amount={}",
                order.getPaymentId(), req.getAmount());
        paymentClientProxy.refund(order.getPaymentId(), refund);
    }
}
