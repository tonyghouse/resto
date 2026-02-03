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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        log.info("Creating order. branchId={} customerName={} itemsCount={}",
                request.getBranchId(), request.getCustomerName(), request.getItems()!=null ? request.getItems().size() : null);

        PriceBreakdown breakdown =
                pricingService.calculate(request.getItems());
        log.debug("Price breakdown calculated. grandTotal={} tax={} itemsTotal={}",
                breakdown.getGrandTotal(), breakdown.getTax(), breakdown.getItemsTotal());


        Order order = new Order();
        UUID branchId = request.getBranchId();
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RestoRestaurantException("Branch not found", HttpStatus.NOT_FOUND));
        log.debug("Branch found. branchId={}", branchId);


        order.setBranch(branch);
        order.setCustomerName(request.getCustomerName());
        order.setCustomerPhone(request.getCustomerPhone());
        order.setStatus(OrderStatus.CREATED);
        order.setTotalAmount(breakdown.getGrandTotal());
        order.setCreatedAt(Instant.now(clock));

        order = orderRepository.save(order);
        log.info("Order created. orderId={} totalAmount={}", order.getId(), order.getTotalAmount());


        for (OrderItemRequest req : request.getItems()) {
            log.debug("Processing order item. type={} id={} qty={}",
                    req.getItemType(), req.getItemId(), req.getQuantity());
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
            log.debug("Order item total calculated. itemName={} lineTotal={}",
                    item.getItemName(), item.getTotalPrice());

            orderItemRepository.save(item);
        }

        log.info("Order creation complete. orderId={} itemsCount={} grandTotal={}",
                order.getId(), request.getItems().size(), breakdown.getGrandTotal());
        return OrderMapper.toResponse(order, breakdown);
    }

    @Override
    public OrderResponse get(UUID orderId) {
        log.debug("Fetching order. orderId={}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        log.debug("Order found. orderId={} totalAmount={}", orderId, order.getTotalAmount());
        PriceBreakdown breakdown = new PriceBreakdown();
        breakdown.setGrandTotal(order.getTotalAmount());
        return OrderMapper.toResponse(order, breakdown);
    }

    @Override
    public PricePreviewResponse preview(CreateOrderRequest request) {
        log.debug("Previewing order price. branchId={} itemsCount={}",
                request.getBranchId(), request.getItems()!=null ? request.getItems().size() : 0);
        PricePreviewResponse response = new PricePreviewResponse();
        response.setBreakdown(
                pricingService.calculate(request.getItems())
        );
        return response;
    }


}
