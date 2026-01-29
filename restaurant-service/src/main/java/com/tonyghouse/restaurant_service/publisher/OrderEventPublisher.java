package com.tonyghouse.restaurant_service.publisher;

import com.tonyghouse.restaurant_service.dto.OrderStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private static final String TOPIC = "order.events";

    private final KafkaTemplate<String, OrderStatusChangedEvent> kafkaTemplate;

    public void publish(OrderStatusChangedEvent event) {
        kafkaTemplate.send(
                TOPIC,
                event.orderId().toString(), // key
                event
        );
    }
}
