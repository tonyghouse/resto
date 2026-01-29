package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.dto.OrderItemRequest;
import com.tonyghouse.restaurant_service.dto.PriceBreakdown;
import com.tonyghouse.restaurant_service.repo.ComboRepository;
import com.tonyghouse.restaurant_service.repo.MenuItemRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderPricingServiceImpl implements OrderPricingService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.18");

    private final MenuItemRepository menuItemRepository;
    private final ComboRepository comboRepository;

    public OrderPricingServiceImpl(
            MenuItemRepository menuItemRepository,
            ComboRepository comboRepository) {
        this.menuItemRepository = menuItemRepository;
        this.comboRepository = comboRepository;
    }

    @Override
    public PriceBreakdown calculate(List<OrderItemRequest> items) {

        BigDecimal itemsTotal = BigDecimal.ZERO;

        for (OrderItemRequest req : items) {

            BigDecimal unitPrice;

            if ("ITEM".equals(req.getItemType())) {
                unitPrice = menuItemRepository.findById(req.getItemId())
                        .orElseThrow(() -> new IllegalArgumentException("Item not found"))
                        .getPrice();

            } else if ("COMBO".equals(req.getItemType())) {
                unitPrice = comboRepository.findById(req.getItemId())
                        .orElseThrow(() -> new IllegalArgumentException("Combo not found"))
                        .getComboPrice();
            } else {
                throw new IllegalArgumentException("Invalid item type");
            }

            itemsTotal = itemsTotal.add(
                    unitPrice.multiply(BigDecimal.valueOf(req.getQuantity()))
            );
        }

        BigDecimal tax = itemsTotal.multiply(TAX_RATE);
        BigDecimal delivery = BigDecimal.ZERO; // hook for later
        BigDecimal grandTotal = itemsTotal.add(tax).add(delivery);

        PriceBreakdown breakdown = new PriceBreakdown();
        breakdown.setItemsTotal(itemsTotal);
        breakdown.setTax(tax);
        breakdown.setDeliveryCharge(delivery);
        breakdown.setGrandTotal(grandTotal);

        return breakdown;
    }
}
