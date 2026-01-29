package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.constants.OrderStatus;
import com.tonyghouse.restaurant_service.dto.OrderStatusHistoryResponse;
import com.tonyghouse.restaurant_service.entity.Order;
import com.tonyghouse.restaurant_service.entity.OrderStatusHistory;
import com.tonyghouse.restaurant_service.exception.RestoRestaurantException;
import com.tonyghouse.restaurant_service.helper.OrderStateRules;
import com.tonyghouse.restaurant_service.repo.OrderRepository;
import com.tonyghouse.restaurant_service.repo.OrderStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderStateServiceImpl implements OrderStateService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final Clock clock;


    @Override
    public void accept(UUID orderId) {
        transition(orderId, OrderStatus.ACCEPTED);
    }

    @Override
    public void markPreparing(UUID orderId) {
        transition(orderId, OrderStatus.PREPARING);
    }

    @Override
    public void markReady(UUID orderId) {
        transition(orderId, OrderStatus.READY);
    }

    @Override
    public void markDelivered(UUID orderId) {
        transition(orderId, OrderStatus.DELIVERED);
    }

    @Override
    public void cancel(UUID orderId) {
        transition(orderId, OrderStatus.CANCELLED);
    }

    private void transition(UUID orderId, OrderStatus target) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RestoRestaurantException("Order not found", HttpStatus.NOT_FOUND));

        OrderStatus current = order.getStatus();

        // idempotency
        if (current == target) {
            return;
        }

        if (!OrderStateRules.canTransition(current, target)) {
            throw new RestoRestaurantException(
                    "Invalid transition: " + current + " → " + target, HttpStatus.BAD_REQUEST);
        }

        order.setStatus(target);
        orderRepository.save(order);

        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setOldStatus(current);
        history.setNewStatus(target);
        history.setChangedAt(Instant.now(clock));

        historyRepository.save(history);
    }

    @Override
    public List<OrderStatusHistoryResponse> history(UUID orderId) {

        return historyRepository
                .findByOrderIdOrderByChangedAtAsc(orderId)
                .stream()
                .map(h -> new OrderStatusHistoryResponse(
                        h.getOldStatus(),
                        h.getNewStatus(),
                        h.getChangedAt()))
                .toList();
    }
}
