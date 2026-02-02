package com.tonyghouse.restaurant_service.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tonyghouse.restaurant_service.constants.OrderStatus;
import com.tonyghouse.restaurant_service.dto.OrderStatusChangedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OrderEventPublisherTest {

    private KafkaTemplate<String, String> kafkaTemplate;
    private OrderEventPublisher publisher;

    @BeforeEach
    void setup() {
        kafkaTemplate = mock(KafkaTemplate.class);
        publisher = new OrderEventPublisher(kafkaTemplate);
    }

    @Test
    void shouldPublishEventToKafka() throws Exception {
        UUID orderId = UUID.randomUUID();

        OrderStatusChangedEvent event =
                new OrderStatusChangedEvent(
                        orderId,
                        OrderStatus.ACCEPTED,
                        OrderStatus.CREATED,
                        Instant.now()
                );

        publisher.publish(event);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);

        verify(kafkaTemplate).send(
                topicCaptor.capture(),
                keyCaptor.capture(),
                payloadCaptor.capture()
        );

        assertThat(topicCaptor.getValue()).isEqualTo("order.events");
        assertThat(keyCaptor.getValue()).isEqualTo(orderId.toString());

        // verify JSON correctness - use same ObjectMapper config as production code
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String expectedPayload = mapper.writeValueAsString(event);

        assertThat(payloadCaptor.getValue()).contains(orderId.toString());
        assertThat(payloadCaptor.getValue()).contains("CREATED");
    }

    @Test
    void shouldNotThrowIfSerializationFails() {
        KafkaTemplate<String, String> template = mock(KafkaTemplate.class);

        doThrow(new RuntimeException("Kafka error"))
                .when(template).send(anyString(), anyString(), anyString());

        OrderEventPublisher publisher = new OrderEventPublisher(template);

        UUID orderId = UUID.randomUUID();
        OrderStatusChangedEvent event = new OrderStatusChangedEvent(
                orderId,
                OrderStatus.CREATED,
                OrderStatus.ACCEPTED,
                Instant.now()
        );

        publisher.publish(event);

        verify(template).send(anyString(), anyString(), anyString());
    }
}