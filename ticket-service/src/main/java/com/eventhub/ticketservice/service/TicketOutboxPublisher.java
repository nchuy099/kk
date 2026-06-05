package com.eventhub.ticketservice.service;

import com.eventhub.common.events.v1.TicketIssuedEventV1;
import com.eventhub.common.messaging.RabbitTopics;
import com.eventhub.ticketservice.domain.TicketOutboxEventStatus;
import com.eventhub.ticketservice.repository.TicketOutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TicketOutboxPublisher {
    private final TicketOutboxEventRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public TicketOutboxPublisher(
            TicketOutboxEventRepository outboxRepository,
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper
    ) {
        this.outboxRepository = outboxRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${app.outbox.publish-interval-ms:5000}")
    @Transactional
    public void publishPendingEvents() {
        var pending = outboxRepository.findTop50ByStatusOrderByCreatedAtAsc(TicketOutboxEventStatus.PENDING);
        for (var event : pending) {
            try {
                var payload = objectMapper.readValue(event.getPayload(), TicketIssuedEventV1.class);
                rabbitTemplate.convertAndSend(RabbitTopics.TICKET_EXCHANGE, RabbitTopics.TICKET_ISSUED_ROUTING_KEY, payload);
                event.markSent();
            } catch (IOException exception) {
                event.markFailed();
            } catch (RuntimeException exception) {
                event.markFailed();
            }
        }
    }
}
