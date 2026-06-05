package com.eventhub.inventoryservice.service;

import com.eventhub.common.events.v1.InventoryReleasedEventV1;
import com.eventhub.common.events.v1.InventoryReserveFailedEventV1;
import com.eventhub.common.events.v1.InventoryReservedEventV1;
import com.eventhub.common.events.v1.ReservationExpiredEventV1;
import com.eventhub.common.messaging.RabbitTopics;
import com.eventhub.inventoryservice.domain.InventoryOutboxEventStatus;
import com.eventhub.inventoryservice.repository.InventoryOutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class InventoryOutboxPublisher {
    private final InventoryOutboxEventRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${app.outbox.publish-interval-ms:5000}")
    @Transactional
    public void publishPendingEvents() {
        var pending = outboxRepository.findTop50ByStatusOrderByCreatedAtAsc(InventoryOutboxEventStatus.PENDING);
        for (var event : pending) {
            try {
                publish(event.getEventType(), event.getPayload());
                event.markSent();
            } catch (IOException exception) {
                event.markFailed();
            } catch (RuntimeException exception) {
                event.markFailed();
            }
        }
    }

    private void publish(String eventType, String payload) throws IOException {
        switch (eventType) {
            case "InventoryReservedEvent" -> rabbitTemplate.convertAndSend(
                    RabbitTopics.SAGA_EXCHANGE,
                    RabbitTopics.INVENTORY_RESERVED_ROUTING_KEY,
                    objectMapper.readValue(payload, InventoryReservedEventV1.class)
            );
            case "InventoryReserveFailedEvent" -> rabbitTemplate.convertAndSend(
                    RabbitTopics.SAGA_EXCHANGE,
                    RabbitTopics.INVENTORY_RESERVE_FAILED_ROUTING_KEY,
                    objectMapper.readValue(payload, InventoryReserveFailedEventV1.class)
            );
            case "InventoryReleasedEvent" -> rabbitTemplate.convertAndSend(
                    RabbitTopics.SAGA_EXCHANGE,
                    RabbitTopics.INVENTORY_RELEASED_ROUTING_KEY,
                    objectMapper.readValue(payload, InventoryReleasedEventV1.class)
            );
            case "ReservationExpiredEvent" -> rabbitTemplate.convertAndSend(
                    RabbitTopics.SAGA_EXCHANGE,
                    RabbitTopics.RESERVATION_EXPIRED_ROUTING_KEY,
                    objectMapper.readValue(payload, ReservationExpiredEventV1.class)
            );
            default -> throw new IllegalStateException("Unsupported outbox event type: " + eventType);
        }
    }
}
