package com.tonyghouse.restaurant_service.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PriceBreakdown {

    private BigDecimal itemsTotal;
    private BigDecimal tax;
    private BigDecimal deliveryCharge;
    private BigDecimal grandTotal;
}
