package com.tonyghouse.restaurant_service.exception;

import com.tonyghouse.restaurant_service.dto.RestoRestaurantError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle already wrapped RestoRestaurantException
     */
    @ExceptionHandler(RestoRestaurantException.class)
    public ResponseEntity<RestoRestaurantError> handleRestoRestaurantException(RestoRestaurantException ex) {
        log.error("RestoRestaurantException", ex);

        RestoRestaurantError response = new RestoRestaurantError(
                ex.getMessage(),
                ex.getErrorCode(),
                Instant.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


    /**
     * Catch-all: EVERY exception is wrapped
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestoRestaurantError> handleAnyException(Exception ex) {
        log.error("Unhandled exception", ex);

        RestoRestaurantException wrapped = new RestoRestaurantException(
                "Something went wrong. Please try again later.",
                "INTERNAL_ERROR",
                ex
        );

        RestoRestaurantError response = new RestoRestaurantError(
                wrapped.getMessage(),
                wrapped.getErrorCode(),
                Instant.now()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestoRestaurantError> handleValidationException(
            MethodArgumentNotValidException ex) {

        log.warn("Validation failed", ex);

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        RestoRestaurantError response = new RestoRestaurantError(
                message,
                "VALIDATION_ERROR",
                Instant.now()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

}
