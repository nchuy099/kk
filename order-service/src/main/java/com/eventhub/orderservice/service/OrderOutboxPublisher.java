package com.eventhub.orderservice.service;

import com.eventhub.common.events.v1.OrderConfirmedEventV1;
import com.eventhub.common.messaging.RabbitTopics;
import com.eventhub.orderservice.domain.OrderOutboxEventStatus;
import com.eventhub.orderservice.repository.OrderOutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrderOutboxPublisher {
    private final OrderOutboxEventRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public OrderOutboxPublisher(
            OrderOutboxEventRepository outboxRepository,
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
        var pending = outboxRepository.findTop50ByStatusOrderByCreatedAtAsc(OrderOutboxEventStatus.PENDING);
        for (var event : pending) {
            try {
                var payload = objectMapper.readValue(event.getPayload(), OrderConfirmedEventV1.class);
                rabbitTemplate.convertAndSend(RabbitTopics.ORDER_EXCHANGE, RabbitTopics.ORDER_CONFIRMED_ROUTING_KEY, payload);
                event.markSent();
            } catch (IOException exception) {
                event.markFailed();
            } catch (RuntimeException exception) {
                event.markFailed();
            }
        }
    }
}
