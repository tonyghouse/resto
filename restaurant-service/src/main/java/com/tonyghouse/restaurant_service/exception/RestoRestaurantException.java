package com.tonyghouse.restaurant_service.exception;

import org.springframework.http.HttpStatus;

public class RestoRestaurantException extends RuntimeException {

    private final String errorCode;

    public RestoRestaurantException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public RestoRestaurantException(String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = String.valueOf(httpStatus.value());
    }

    public RestoRestaurantException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
