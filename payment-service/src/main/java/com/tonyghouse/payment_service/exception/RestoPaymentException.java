package com.tonyghouse.payment_service.exception;

import org.springframework.http.HttpStatus;

public class RestoPaymentException extends RuntimeException {

    private final String errorCode;

    public RestoPaymentException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public RestoPaymentException(String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = String.valueOf(httpStatus.value());
    }

    public RestoPaymentException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
