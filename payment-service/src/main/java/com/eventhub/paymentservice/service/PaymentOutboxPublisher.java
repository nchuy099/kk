package com.eventhub.paymentservice.service;

import com.eventhub.common.events.v1.PaymentSucceededEventV1;
import com.eventhub.common.messaging.RabbitTopics;
import com.eventhub.paymentservice.domain.PaymentOutboxEventStatus;
import com.eventhub.paymentservice.repository.PaymentOutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentOutboxPublisher {
    private final PaymentOutboxEventRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public PaymentOutboxPublisher(
            PaymentOutboxEventRepository outboxRepository,
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
        var pending = outboxRepository.findTop50ByStatusOrderByCreatedAtAsc(PaymentOutboxEventStatus.PENDING);
        for (var event : pending) {
            try {
                var payload = objectMapper.readValue(event.getPayload(), PaymentSucceededEventV1.class);
                rabbitTemplate.convertAndSend(RabbitTopics.PAYMENT_EXCHANGE, RabbitTopics.PAYMENT_SUCCESS_ROUTING_KEY, payload);
                event.markSent();
            } catch (IOException exception) {
                event.markFailed();
            } catch (RuntimeException exception) {
                event.markFailed();
            }
        }
    }
}
