package com.tonyghouse.payment_service.service;

import com.tonyghouse.payment_service.dto.RefundRequest;
import com.tonyghouse.payment_service.entity.Refund;

import java.util.UUID;

public interface RefundService {

    Refund refund(
            UUID paymentId,
            RefundRequest request
    );
}
