package com.tonyghouse.restaurant_service.constants.payment;

public enum PaymentStatus {
    INITIATED,
    PROCESSING,
    SUCCESS,
    FAILED,
    RETRYING,
    FAILED_PERMANENT,
    REFUNDED
}
