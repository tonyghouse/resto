package com.tonyghouse.restaurant_service.helper;

import com.tonyghouse.restaurant_service.constants.OrderStatus;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.assertThat;
import static com.tonyghouse.restaurant_service.constants.OrderStatus.*;

class OrderStateRulesTest {

    // VALID TRANSITIONS (true)
    @Test
    void shouldAllowCreatedTransitions() {
        assertThat(OrderStateRules.canTransition(CREATED, ACCEPTED)).isTrue();
        assertThat(OrderStateRules.canTransition(CREATED, CANCELLED)).isTrue();
    }

    @Test
    void shouldAllowAcceptedTransitions() {
        assertThat(OrderStateRules.canTransition(ACCEPTED, PREPARING)).isTrue();
        assertThat(OrderStateRules.canTransition(ACCEPTED, CANCELLED)).isTrue();
    }

    @Test
    void shouldAllowPreparingTransitions() {
        assertThat(OrderStateRules.canTransition(PREPARING, READY)).isTrue();
        assertThat(OrderStateRules.canTransition(PREPARING, CANCELLED)).isTrue();
    }

    @Test
    void shouldAllowReadyTransition() {
        assertThat(OrderStateRules.canTransition(READY, DELIVERED)).isTrue();
    }

    // INVALID TRANSITIONS (false)
    @Test
    void shouldRejectInvalidTransitions() {
        assertThat(OrderStateRules.canTransition(CREATED, READY)).isFalse();
        assertThat(OrderStateRules.canTransition(READY, ACCEPTED)).isFalse();
        assertThat(OrderStateRules.canTransition(DELIVERED, CREATED)).isFalse();
        assertThat(OrderStateRules.canTransition(CANCELLED, ACCEPTED)).isFalse();
    }

    @Test
    void terminalStatesShouldNotAllowFurtherTransitions() {
        for (OrderStatus status : new OrderStatus[]{DELIVERED, CANCELLED}) {
            for (OrderStatus next : OrderStatus.values()) {
                assertThat(OrderStateRules.canTransition(status, next)).isFalse();
            }
        }
    }
    @Test
    void shouldReturnFalseWhenFromIsNull() {
        assertThat(OrderStateRules.canTransition(null, CREATED)).isFalse();
    }


    @Test
    void privateConstructorShouldBeInvocableForCoverage() throws Exception {
        Constructor<OrderStateRules> constructor =
                OrderStateRules.class.getDeclaredConstructor();

        constructor.setAccessible(true);
        constructor.newInstance();
    }
}
