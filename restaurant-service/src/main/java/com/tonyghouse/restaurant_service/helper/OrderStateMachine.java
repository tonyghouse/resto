package com.tonyghouse.restaurant_service.helper;

import com.tonyghouse.restaurant_service.constants.OrderStatus;

import java.util.Map;
import java.util.Set;

public final class OrderStateMachine {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS =
            Map.of(
                OrderStatus.CREATED,
                    Set.of(OrderStatus.ACCEPTED, OrderStatus.CANCELLED),

                OrderStatus.ACCEPTED,
                    Set.of(OrderStatus.PREPARING, OrderStatus.CANCELLED),

                OrderStatus.PREPARING,
                    Set.of(OrderStatus.READY, OrderStatus.CANCELLED),

                OrderStatus.READY,
                    Set.of(OrderStatus.DELIVERED),

                OrderStatus.DELIVERED,
                    Set.of(),

                OrderStatus.CANCELLED,
                    Set.of()
            );

    private OrderStateMachine() {}

    public static boolean canTransition(
            OrderStatus from,
            OrderStatus to
    ) {
        return ALLOWED_TRANSITIONS
                .getOrDefault(from, Set.of())
                .contains(to);
    }
}
