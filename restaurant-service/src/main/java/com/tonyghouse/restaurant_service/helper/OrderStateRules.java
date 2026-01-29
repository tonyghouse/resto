package com.tonyghouse.restaurant_service.helper;

import com.tonyghouse.restaurant_service.constants.OrderStatus;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import static com.tonyghouse.restaurant_service.constants.OrderStatus.*;

/*
CREATED
  |-> ACCEPTED
  |     |-> PREPARING
  |     |     |-> READY
  |     |     |     -> DELIVERED
  |     |     -> CANCELLED
  |     -> CANCELLED
  -> CANCELLED
 */
public final class OrderStateRules {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED;

    static {
        Map<OrderStatus, Set<OrderStatus>> map = new EnumMap<>(OrderStatus.class);

        map.put(CREATED, Set.of(ACCEPTED, CANCELLED));
        map.put(ACCEPTED, Set.of(PREPARING, CANCELLED));
        map.put(PREPARING, Set.of(READY, CANCELLED));
        map.put(READY, Set.of(DELIVERED));
        map.put(DELIVERED, Set.of());
        map.put(CANCELLED, Set.of());

        ALLOWED = Map.copyOf(map);
    }

    public static boolean canTransition(
            OrderStatus from,
            OrderStatus to) {
        return ALLOWED.getOrDefault(from, Set.of()).contains(to);
    }

    private OrderStateRules() {}
}
