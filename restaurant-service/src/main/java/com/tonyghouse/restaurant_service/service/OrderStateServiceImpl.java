package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.constants.OrderStatus;
import com.tonyghouse.restaurant_service.dto.OrderStatusChangedEvent;
import com.tonyghouse.restaurant_service.dto.OrderStatusHistoryResponse;
import com.tonyghouse.restaurant_service.entity.Order;
import com.tonyghouse.restaurant_service.entity.OrderStatusHistory;
import com.tonyghouse.restaurant_service.exception.RestoRestaurantException;
import com.tonyghouse.restaurant_service.helper.OrderStateRules;
import com.tonyghouse.restaurant_service.publisher.OrderEventPublisher;
import com.tonyghouse.restaurant_service.repo.OrderRepository;
import com.tonyghouse.restaurant_service.repo.OrderStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class OrderStateServiceImpl implements OrderStateService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final Clock clock;
    private final OrderEventPublisher orderEventPublisher;


    @Override
    public void accept(UUID orderId) {
        log.info("Accept requested. orderId={}", orderId);
        transition(orderId, OrderStatus.ACCEPTED);
    }

    @Override
    public void markPreparing(UUID orderId) {
        log.info("Preparing requested. orderId={}", orderId);
        transition(orderId, OrderStatus.PREPARING);
    }

    @Override
    public void markReady(UUID orderId) {
        log.info("Ready requested. orderId={}", orderId);
        transition(orderId, OrderStatus.READY);
    }

    @Override
    public void markDelivered(UUID orderId) {
        log.info("Delivered requested. orderId={}", orderId);
        transition(orderId, OrderStatus.DELIVERED);
    }

    @Override
    public void cancel(UUID orderId) {
        log.info("Cancel requested. orderId={}", orderId);
        transition(orderId, OrderStatus.CANCELLED);
    }

    private void transition(UUID orderId, OrderStatus target) {
        log.debug("Transition started. orderId={} targetStatus={}", orderId, target);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RestoRestaurantException("Order not found", HttpStatus.NOT_FOUND));

        OrderStatus current = order.getStatus();

        // idempotency
        if (current == target) {
            log.info("Transition skipped (idempotent). orderId={} status={}", orderId, current);
            return;
        }

        if (!OrderStateRules.canTransition(current, target)) {
            log.warn("Invalid transition attempted. orderId={} from={} to={}",
                    orderId, current, target);
            throw new RestoRestaurantException(
                    "Invalid transition: " + current + " → " + target, HttpStatus.BAD_REQUEST);
        }

        order.setStatus(target);
        orderRepository.save(order);
        log.debug("Order saved with new status. orderId={}", orderId);


        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setOldStatus(current);
        history.setNewStatus(target);
        history.setChangedAt(Instant.now(clock));

        historyRepository.save(history);
        log.debug("Status history recorded. orderId={} old={} new={} at={}",
                orderId, current, target, history.getChangedAt());
        orderEventPublisher.publish(
                new OrderStatusChangedEvent(
                        order.getId(),
                        current,
                        target,
                        history.getChangedAt()
                )
        );
    }

    @Override
    public List<OrderStatusHistoryResponse> history(UUID orderId) {
        log.debug("Fetching order status history. orderId={}", orderId);
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
