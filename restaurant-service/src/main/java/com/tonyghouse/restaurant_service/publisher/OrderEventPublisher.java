package com.tonyghouse.restaurant_service.publisher;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tonyghouse.restaurant_service.dto.OrderStatusChangedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private static final String TOPIC = "order.events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public void publish(OrderStatusChangedEvent event) {

        try {
            String payload = objectMapper.writeValueAsString(event);

            kafkaTemplate.send(
                    TOPIC,
                    event.orderId().toString(),
                    payload
            );
            log.info("Event published: {}", payload);

        } catch (Exception e) {
        }
    }
}
