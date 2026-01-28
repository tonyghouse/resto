package com.tonyghouse.payment_service.dto;

import java.math.BigDecimal;

public class PaymentItemResponse {

    private String itemName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}
