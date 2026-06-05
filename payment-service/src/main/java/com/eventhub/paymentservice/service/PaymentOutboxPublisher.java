package com.eventhub.paymentservice.service;

import com.eventhub.common.events.v1.PaymentCreatedEventV1;
import com.eventhub.common.events.v1.PaymentFailedEventV1;
import com.eventhub.common.events.v1.PaymentRefundFailedEventV1;
import com.eventhub.common.events.v1.PaymentRefundedEventV1;
import com.eventhub.common.events.v1.PaymentSucceededEventV1;
import com.eventhub.common.messaging.RabbitTopics;
import com.eventhub.paymentservice.domain.PaymentOutboxEventStatus;
import com.eventhub.paymentservice.repository.PaymentOutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentOutboxPublisher {
    private final PaymentOutboxEventRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${app.outbox.publish-interval-ms:5000}")
    @Transactional
    public void publishPendingEvents() {
        var pending = outboxRepository.findTop50ByStatusOrderByCreatedAtAsc(PaymentOutboxEventStatus.PENDING);
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
            case "PaymentCreatedEvent" -> rabbitTemplate.convertAndSend(
                    RabbitTopics.SAGA_EXCHANGE,
                    RabbitTopics.PAYMENT_CREATED_ROUTING_KEY,
                    objectMapper.readValue(payload, PaymentCreatedEventV1.class)
            );
            case "PaymentSucceededEvent" -> rabbitTemplate.convertAndSend(
                    RabbitTopics.SAGA_EXCHANGE,
                    RabbitTopics.PAYMENT_SUCCEEDED_ROUTING_KEY,
                    objectMapper.readValue(payload, PaymentSucceededEventV1.class)
            );
            case "PaymentFailedEvent" -> rabbitTemplate.convertAndSend(
                    RabbitTopics.SAGA_EXCHANGE,
                    RabbitTopics.PAYMENT_FAILED_ROUTING_KEY,
                    objectMapper.readValue(payload, PaymentFailedEventV1.class)
            );
            case "PaymentRefundedEvent" -> rabbitTemplate.convertAndSend(
                    RabbitTopics.SAGA_EXCHANGE,
                    RabbitTopics.PAYMENT_REFUNDED_ROUTING_KEY,
                    objectMapper.readValue(payload, PaymentRefundedEventV1.class)
            );
            case "PaymentRefundFailedEvent" -> rabbitTemplate.convertAndSend(
                    RabbitTopics.SAGA_EXCHANGE,
                    RabbitTopics.PAYMENT_REFUND_FAILED_ROUTING_KEY,
                    objectMapper.readValue(payload, PaymentRefundFailedEventV1.class)
            );
            default -> throw new IllegalStateException("Unsupported outbox event type: " + eventType);
        }
    }
}
