package com.tonyghouse.payment_service.exception;

public class InvalidRefundException extends RuntimeException {
    public InvalidRefundException(String message) {
        super(message);
    }
}
