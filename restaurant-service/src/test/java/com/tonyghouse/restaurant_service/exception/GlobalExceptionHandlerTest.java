package com.tonyghouse.restaurant_service.exception;

import com.tonyghouse.restaurant_service.dto.RestoRestaurantError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    private Clock clock;
    private Instant fixedInstant;

    @BeforeEach
    void setUp() {
        fixedInstant = Instant.parse("2026-01-01T10:00:00Z");

        clock = mock(Clock.class);
        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);

        handler = new GlobalExceptionHandler(clock);
    }

    @Test
    void shouldReturnBadRequestForRestoRestaurantException() {
        RestoRestaurantException ex =
                new RestoRestaurantException("Restaurant failed", "400");

        ResponseEntity<RestoRestaurantError> response =
                handler.handleRestoRestaurantException(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(400);

        RestoRestaurantError body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).isEqualTo("Restaurant failed");
        assertThat(body.getCode()).isEqualTo("400");
        assertThat(body.getTimestamp()).isEqualTo(fixedInstant);
    }

    @Test
    void shouldWrapAnyExceptionAsInternalError() {
        Exception ex = new RuntimeException("boom");

        ResponseEntity<RestoRestaurantError> response =
                handler.handleAnyException(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(500);

        RestoRestaurantError body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage())
                .isEqualTo("Something went wrong. Please try again later.");
        assertThat(body.getCode()).isEqualTo("INTERNAL_ERROR");
        assertThat(body.getTimestamp()).isEqualTo(fixedInstant);
    }

    @Test
    void shouldReturnValidationErrorMessage() {
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError =
                new FieldError("request", "name", "must not be blank");

        when(bindingResult.getFieldErrors())
                .thenReturn(List.of(fieldError));

        MethodArgumentNotValidException ex =
                mock(MethodArgumentNotValidException.class);

        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<RestoRestaurantError> response =
                handler.handleValidationException(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(400);

        RestoRestaurantError body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).isEqualTo("name: must not be blank");
        assertThat(body.getCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(body.getTimestamp()).isEqualTo(fixedInstant);
    }

    @Test
    void shouldReturn403ForAccessDeniedException() {
        ResponseEntity<?> response =
                handler.handleAccessDenied(new AccessDeniedException("denied"));

        assertThat(response.getStatusCodeValue()).isEqualTo(403);
        assertThat(response.getBody()).isNull();
    }

}
