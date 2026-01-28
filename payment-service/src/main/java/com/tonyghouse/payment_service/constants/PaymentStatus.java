package com.tonyghouse.payment_service.constants;

public enum PaymentStatus {
    INITIATED,
    PROCESSING,
    SUCCESS,
    FAILED,
    RETRYING,
    FAILED_PERMANENT,
    REFUNDED
}
