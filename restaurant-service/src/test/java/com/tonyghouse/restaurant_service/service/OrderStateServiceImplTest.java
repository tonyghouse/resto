package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.constants.OrderStatus;
import com.tonyghouse.restaurant_service.dto.OrderStatusChangedEvent;
import com.tonyghouse.restaurant_service.entity.Order;
import com.tonyghouse.restaurant_service.entity.OrderStatusHistory;
import com.tonyghouse.restaurant_service.exception.RestoRestaurantException;
import com.tonyghouse.restaurant_service.publisher.OrderEventPublisher;
import com.tonyghouse.restaurant_service.repo.OrderRepository;
import com.tonyghouse.restaurant_service.repo.OrderStatusHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderStateServiceImplTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    OrderStatusHistoryRepository historyRepository;

    @Mock
    OrderEventPublisher orderEventPublisher;

    @Mock
    Clock clock;

    @InjectMocks
    OrderStateServiceImpl service;

    @BeforeEach
    void setup() {
        Mockito.when(clock.instant())
                .thenReturn(Instant.parse("2025-01-01T00:00:00Z"));
    }

    @Test
    void accept_success() {
        UUID orderId = UUID.randomUUID();

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.CREATED);

        Mockito.when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));
        Mockito.when(orderRepository.save(order))
                .thenReturn(order);

        service.accept(orderId);

        assertEquals(OrderStatus.ACCEPTED, order.getStatus());
        Mockito.verify(historyRepository).save(Mockito.any(OrderStatusHistory.class));
        Mockito.verify(orderEventPublisher)
                .publish(Mockito.any(OrderStatusChangedEvent.class));
    }

    @Test
    void transition_idempotent() {
        UUID orderId = UUID.randomUUID();

        Order order = new Order();
        order.setStatus(OrderStatus.ACCEPTED);

        Mockito.when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        service.accept(orderId);

        Mockito.verify(orderRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(historyRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(orderEventPublisher, Mockito.never()).publish(Mockito.any());
    }

    @Test
    void transition_invalid() {
        UUID orderId = UUID.randomUUID();

        Order order = new Order();
        order.setStatus(OrderStatus.CREATED);

        Mockito.when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        assertThrows(RestoRestaurantException.class,
                () -> service.markDelivered(orderId));
    }

    @Test
    void history_success() {
        UUID orderId = UUID.randomUUID();

        OrderStatusHistory h1 = new OrderStatusHistory();
        h1.setOldStatus(OrderStatus.CREATED);
        h1.setNewStatus(OrderStatus.ACCEPTED);
        h1.setChangedAt(Instant.parse("2025-01-01T00:00:00Z"));

        Mockito.when(historyRepository.findByOrderIdOrderByChangedAtAsc(orderId))
                .thenReturn(List.of(h1));

        var res = service.history(orderId);

        assertEquals(1, res.size());
        assertEquals(OrderStatus.CREATED, res.get(0).getOldStatus());
        assertEquals(OrderStatus.ACCEPTED, res.get(0).getNewStatus());
    }
}
