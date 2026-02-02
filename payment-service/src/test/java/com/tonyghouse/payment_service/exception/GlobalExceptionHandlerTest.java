package com.tonyghouse.payment_service.exception;

import com.tonyghouse.payment_service.dto.RestoPaymentError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void shouldReturnBadRequestForRestoPaymentException() {
        RestoPaymentException ex =
                new RestoPaymentException("Payment failed", "500");

        ResponseEntity<RestoPaymentError> response =
                handler.handleRestoPaymentException(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(400);

        RestoPaymentError body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).isEqualTo("Payment failed");
        assertThat(body.getCode()).isEqualTo("500");
        assertThat(body.getTimestamp()).isNotNull();
    }

    @Test
    void shouldWrapAnyExceptionAsInternalError() {
        Exception ex = new RuntimeException("failed");

        ResponseEntity<RestoPaymentError> response =
                handler.handleAnyException(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(500);

        RestoPaymentError body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage())
                .isEqualTo("Something went wrong. Please try again later.");
        assertThat(body.getCode()).isEqualTo("INTERNAL_ERROR");
    }


    @Test
    void shouldReturnValidationErrorMessage() {
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError =
                new FieldError("request", "amount", "must be positive");

        when(bindingResult.getFieldErrors())
                .thenReturn(List.of(fieldError));

        MethodArgumentNotValidException ex =
                mock(MethodArgumentNotValidException.class);

        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<RestoPaymentError> response =
                handler.handleValidationException(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(400);

        RestoPaymentError body = response.getBody();
        assertThat(body.getMessage()).isEqualTo("amount: must be positive");
        assertThat(body.getCode()).isEqualTo("VALIDATION_ERROR");
    }


    @Test
    void shouldReturn403ForAccessDeniedException() {
        ResponseEntity<?> response =
                handler.handleAccessDenied(new AccessDeniedException("denied"));

        assertThat(response.getStatusCodeValue()).isEqualTo(403);
        assertThat(response.getBody()).isNull();
    }

}
