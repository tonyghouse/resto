package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.constants.OrderStatus;
import com.tonyghouse.restaurant_service.dto.BranchResponse;
import com.tonyghouse.restaurant_service.dto.CreateOrderRequest;
import com.tonyghouse.restaurant_service.dto.OrderItemRequest;
import com.tonyghouse.restaurant_service.dto.OrderResponse;
import com.tonyghouse.restaurant_service.dto.PriceBreakdown;
import com.tonyghouse.restaurant_service.dto.PricePreviewResponse;
import com.tonyghouse.restaurant_service.entity.Branch;
import com.tonyghouse.restaurant_service.entity.Combo;
import com.tonyghouse.restaurant_service.entity.MenuItem;
import com.tonyghouse.restaurant_service.entity.Order;
import com.tonyghouse.restaurant_service.entity.OrderItem;
import com.tonyghouse.restaurant_service.exception.RestoRestaurantException;
import com.tonyghouse.restaurant_service.mapper.OrderMapper;
import com.tonyghouse.restaurant_service.repo.BranchRepository;
import com.tonyghouse.restaurant_service.repo.ComboRepository;
import com.tonyghouse.restaurant_service.repo.MenuItemRepository;
import com.tonyghouse.restaurant_service.repo.OrderItemRepository;
import com.tonyghouse.restaurant_service.repo.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderPricingService pricingService;
    private final MenuItemRepository menuItemRepository;
    private final ComboRepository comboRepository;
    private final BranchRepository branchRepository;
    private final Clock clock;

    @Override
    public OrderResponse create(CreateOrderRequest request) {

        PriceBreakdown breakdown =
                pricingService.calculate(request.getItems());

        Order order = new Order();
        UUID branchId = request.getBranchId();
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RestoRestaurantException("Branch not found", HttpStatus.NOT_FOUND));

        order.setBranch(branch);
        order.setCustomerName(request.getCustomerName());
        order.setCustomerPhone(request.getCustomerPhone());
        order.setStatus(OrderStatus.CREATED);
        order.setTotalAmount(breakdown.getGrandTotal());
        order.setCreatedAt(Instant.now(clock));

        order = orderRepository.save(order);

        for (OrderItemRequest req : request.getItems()) {

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setItemType(req.getItemType());
            item.setQuantity(req.getQuantity());
            item.setSpecialNotes(req.getSpecialNotes());

            if ("ITEM".equals(req.getItemType())) {
                MenuItem mi = menuItemRepository.findById(req.getItemId()).orElseThrow();
                item.setItemName(mi.getName());
                item.setUnitPrice(mi.getPrice());

            } else {
                Combo combo = comboRepository.findById(req.getItemId()).orElseThrow();
                item.setItemName(combo.getName());
                item.setUnitPrice(combo.getComboPrice());
            }

            item.setTotalPrice(
                    item.getUnitPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity()))
            );

            orderItemRepository.save(item);
        }

        return OrderMapper.toResponse(order, breakdown);
    }

    @Override
    public OrderResponse get(UUID orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        PriceBreakdown breakdown = new PriceBreakdown();
        breakdown.setGrandTotal(order.getTotalAmount());

        return OrderMapper.toResponse(order, breakdown);
    }

    @Override
    public PricePreviewResponse preview(CreateOrderRequest request) {

        PricePreviewResponse response = new PricePreviewResponse();
        response.setBreakdown(
                pricingService.calculate(request.getItems())
        );
        return response;
    }


}
