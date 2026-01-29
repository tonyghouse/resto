package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.dto.OrderItemRequest;
import com.tonyghouse.restaurant_service.dto.PriceBreakdown;
import com.tonyghouse.restaurant_service.entity.Order;
import com.tonyghouse.restaurant_service.exception.RestoRestaurantException;
import com.tonyghouse.restaurant_service.repo.ComboRepository;
import com.tonyghouse.restaurant_service.repo.MenuItemRepository;
import org.springframework.http.HttpStatus;
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
                        .orElseThrow(() -> new RestoRestaurantException("Item not found", HttpStatus.NOT_FOUND))
                        .getPrice();

            } else if ("COMBO".equals(req.getItemType())) {
                unitPrice = comboRepository.findById(req.getItemId())
                        .orElseThrow(() -> new RestoRestaurantException("Combo not found", HttpStatus.NOT_FOUND))
                        .getComboPrice();
            } else {
                throw new RestoRestaurantException("Invalid item type",HttpStatus.BAD_REQUEST);
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

    @Override
    public PriceBreakdown recalculateFromOrder(Order order) {

        if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
            throw new RestoRestaurantException(
                    "Order has no items to recalculate",
                    HttpStatus.BAD_REQUEST
            );
        }

        final BigDecimal[] itemsTotal = {BigDecimal.ZERO};

        order.getItems().forEach(item -> {

            if (item.getUnitPrice() == null || item.getQuantity() <= 0) {
                throw new RestoRestaurantException(
                        "Invalid order item data for recalculation",
                        HttpStatus.INTERNAL_SERVER_ERROR
                );
            }

            BigDecimal lineTotal = item.getUnitPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));

            itemsTotal[0] = itemsTotal[0].add(lineTotal);
        });

        BigDecimal tax = itemsTotal[0].multiply(TAX_RATE);
        BigDecimal delivery = BigDecimal.ZERO; // future extension
        BigDecimal grandTotal = itemsTotal[0].add(tax).add(delivery);

        PriceBreakdown breakdown = new PriceBreakdown();
        breakdown.setItemsTotal(itemsTotal[0]);
        breakdown.setTax(tax);
        breakdown.setDeliveryCharge(delivery);
        breakdown.setGrandTotal(grandTotal);

        return breakdown;
    }

}
