package com.tonyghouse.payment_service.mapper;

import com.tonyghouse.payment_service.dto.CreatePaymentResponse;
import com.tonyghouse.payment_service.dto.PaymentResponse;
import com.tonyghouse.payment_service.dto.RefundResponse;
import com.tonyghouse.payment_service.entity.Payment;
import com.tonyghouse.payment_service.entity.Refund;

import java.util.UUID;


public class PaymentMapper {
    private PaymentMapper(){
    }

    public static CreatePaymentResponse mapToCreatePaymentResponse(Payment payment) {
        CreatePaymentResponse createPaymentResponse = new CreatePaymentResponse();
        createPaymentResponse.setPaymentId(payment.getPaymentId());
        createPaymentResponse.setOrderId(payment.getOrderId());
        createPaymentResponse.setStatus(payment.getStatus());
        createPaymentResponse.setPayableAmount(payment.getPayableAmount());
        createPaymentResponse.setCurrency(payment.getCurrency());
        createPaymentResponse.setCreatedAt(payment.getCreatedAt());
        return createPaymentResponse;
    }

    public static PaymentResponse toPaymentResponse(Payment payment) {
        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setPaymentId(payment.getPaymentId());
        paymentResponse.setOrderId(payment.getOrderId());
        paymentResponse.setMethod(payment.getMethod());
        paymentResponse.setStatus(payment.getStatus());
        paymentResponse.setTotalAmount(payment.getTotalAmount());
        paymentResponse.setTaxAmount(payment.getTaxAmount());
        paymentResponse.setPayableAmount(payment.getPayableAmount());
        paymentResponse.setCurrency(payment.getCurrency());
        paymentResponse.setCreatedAt(payment.getCreatedAt());
        paymentResponse.setUpdatedAt(payment.getUpdatedAt());
        return paymentResponse;
    }

    public static RefundResponse toRefundResponse(Refund refund, UUID paymentId) {
        RefundResponse refundResponse = new RefundResponse();
        refundResponse.setRefundId(refund.getRefundId());
        refundResponse.setPaymentId(paymentId);
        refundResponse.setRefundAmount(refund.getRefundAmount());
        refundResponse.setStatus(refund.getStatus());
        refundResponse.setCreatedAt(refund.getCreatedAt());
        return refundResponse;
    }
}
