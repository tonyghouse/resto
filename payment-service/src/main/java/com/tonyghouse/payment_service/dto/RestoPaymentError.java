package com.tonyghouse.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class RestoPaymentError {
    private String message;
    private String code;
    private Instant timestamp;
}
