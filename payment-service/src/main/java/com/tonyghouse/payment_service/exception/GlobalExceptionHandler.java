package com.tonyghouse.payment_service.exception;

import com.tonyghouse.payment_service.dto.RestoPaymentError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle already wrapped RestoPaymentException
     */
    @ExceptionHandler(RestoPaymentException.class)
    public ResponseEntity<RestoPaymentError> handleRestoPaymentException(RestoPaymentException ex) {
        log.error("RestoPaymentException", ex);

        RestoPaymentError response = new RestoPaymentError(
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
    public ResponseEntity<RestoPaymentError> handleAnyException(Exception ex) {
        log.error("Unhandled exception", ex);

        RestoPaymentException wrapped = new RestoPaymentException(
                "Something went wrong. Please try again later.",
                "INTERNAL_ERROR",
                ex
        );

        RestoPaymentError response = new RestoPaymentError(
                wrapped.getMessage(),
                wrapped.getErrorCode(),
                Instant.now()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestoPaymentError> handleValidationException(
            MethodArgumentNotValidException ex) {

        log.warn("Validation failed", ex);

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        RestoPaymentError response = new RestoPaymentError(
                message,
                "VALIDATION_ERROR",
                Instant.now()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler({
            AccessDeniedException.class,
            AuthorizationDeniedException.class
    })
    public ResponseEntity<?> handleAccessDenied(Exception ex) {
        return ResponseEntity.status(403).build();
    }

}
