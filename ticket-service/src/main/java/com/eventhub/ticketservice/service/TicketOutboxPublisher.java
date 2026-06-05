package com.eventhub.ticketservice.service;

import com.eventhub.common.events.v1.TicketIssueFailedEventV1;
import com.eventhub.common.events.v1.TicketIssuedEventV1;
import com.eventhub.common.messaging.RabbitTopics;
import com.eventhub.ticketservice.domain.TicketOutboxEventStatus;
import com.eventhub.ticketservice.repository.TicketOutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TicketOutboxPublisher {
    private final TicketOutboxEventRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${app.outbox.publish-interval-ms:5000}")
    @Transactional
    public void publishPendingEvents() {
        var pending = outboxRepository.findTop50ByStatusOrderByCreatedAtAsc(TicketOutboxEventStatus.PENDING);
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
            case "TicketIssuedEvent" -> rabbitTemplate.convertAndSend(
                    RabbitTopics.SAGA_EXCHANGE,
                    RabbitTopics.TICKET_ISSUED_ROUTING_KEY,
                    objectMapper.readValue(payload, TicketIssuedEventV1.class)
            );
            case "TicketIssueFailedEvent" -> rabbitTemplate.convertAndSend(
                    RabbitTopics.SAGA_EXCHANGE,
                    RabbitTopics.TICKET_ISSUE_FAILED_ROUTING_KEY,
                    objectMapper.readValue(payload, TicketIssueFailedEventV1.class)
            );
            default -> throw new IllegalStateException("Unsupported outbox event type: " + eventType);
        }
    }
}
