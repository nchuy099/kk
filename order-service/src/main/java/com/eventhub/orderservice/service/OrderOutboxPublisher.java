package com.eventhub.orderservice.service;

import com.eventhub.common.events.v1.InventoryReserveFailedEventV1;
import com.eventhub.common.events.v1.InventoryReservedEventV1;
import com.eventhub.common.events.v1.OrderCancelledEventV1;
import com.eventhub.common.events.v1.OrderCompensatingEventV1;
import com.eventhub.common.events.v1.OrderCompletedEventV1;
import com.eventhub.common.events.v1.OrderCompensationFailedEventV1;
import com.eventhub.common.events.v1.OrderCreatedEventV1;
import com.eventhub.common.events.v1.OrderConfirmedEventV1;
import com.eventhub.common.events.v1.OrderExpiredEventV1;
import com.eventhub.common.events.v1.OrderRefundedEventV1;
import com.eventhub.common.events.v1.PaymentCreatedEventV1;
import com.eventhub.common.events.v1.PaymentFailedEventV1;
import com.eventhub.common.events.v1.PaymentRefundFailedEventV1;
import com.eventhub.common.events.v1.PaymentRefundRequestedEventV1;
import com.eventhub.common.events.v1.PaymentRefundedEventV1;
import com.eventhub.common.events.v1.PaymentRequestedEventV1;
import com.eventhub.common.events.v1.PaymentSucceededEventV1;
import com.eventhub.common.events.v1.ReservationExpiredEventV1;
import com.eventhub.common.events.v1.TicketIssueFailedEventV1;
import com.eventhub.common.events.v1.TicketIssueRequestedEventV1;
import com.eventhub.common.events.v1.TicketIssuedEventV1;
import com.eventhub.common.messaging.RabbitTopics;
import com.eventhub.orderservice.domain.OrderOutboxEventStatus;
import com.eventhub.orderservice.repository.OrderOutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OrderOutboxPublisher {
    private final OrderOutboxEventRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${app.outbox.publish-interval-ms:5000}")
    @Transactional
    public void publishPendingEvents() {
        var pending = outboxRepository.findTop50ByStatusOrderByCreatedAtAsc(OrderOutboxEventStatus.PENDING);
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
            case "OrderCreatedEvent" -> rabbitTemplate.convertAndSend(
                    RabbitTopics.SAGA_EXCHANGE,
                    RabbitTopics.ORDER_CREATED_ROUTING_KEY,
                    objectMapper.readValue(payload, OrderCreatedEventV1.class)
            );
            case "PaymentRequestedEvent" -> rabbitTemplate.convertAndSend(
                    RabbitTopics.SAGA_EXCHANGE,
                    RabbitTopics.PAYMENT_REQUESTED_ROUTING_KEY,
                    objectMapper.readValue(payload, PaymentRequestedEventV1.class)
            );
            case "OrderConfirmedEvent" -> rabbitTemplate.convertAndSend(
                    RabbitTopics.SAGA_EXCHANGE,
                    RabbitTopics.ORDER_CONFIRMED_ROUTING_KEY,
                    objectMapper.readValue(payload, OrderConfirmedEventV1.class)
            );
            case "TicketIssueRequestedEvent" -> rabbitTemplate.convertAndSend(
                    RabbitTopics.SAGA_EXCHANGE,
                    RabbitTopics.TICKET_ISSUE_REQUESTED_ROUTING_KEY,
                    objectMapper.readValue(payload, TicketIssueRequestedEventV1.class)
            );
            case "OrderCancelledEvent" -> rabbitTemplate.convertAndSend(
                    RabbitTopics.SAGA_EXCHANGE,
                    RabbitTopics.ORDER_CANCELLED_ROUTING_KEY,
                    objectMapper.readValue(payload, OrderCancelledEventV1.class)
            );
            case "OrderExpiredEvent" -> rabbitTemplate.convertAndSend(
                    RabbitTopics.SAGA_EXCHANGE,
                    RabbitTopics.ORDER_EXPIRED_ROUTING_KEY,
                    objectMapper.readValue(payload, OrderExpiredEventV1.class)
            );
            case "OrderCompletedEvent" -> rabbitTemplate.convertAndSend(
                    RabbitTopics.SAGA_EXCHANGE,
                    RabbitTopics.ORDER_COMPLETED_ROUTING_KEY,
                    objectMapper.readValue(payload, OrderCompletedEventV1.class)
            );
            case "OrderCompensatingEvent" -> rabbitTemplate.convertAndSend(
                    RabbitTopics.SAGA_EXCHANGE,
                    RabbitTopics.ORDER_COMPENSATING_ROUTING_KEY,
                    objectMapper.readValue(payload, OrderCompensatingEventV1.class)
            );
            case "PaymentRefundRequestedEvent" -> rabbitTemplate.convertAndSend(
                    RabbitTopics.SAGA_EXCHANGE,
                    RabbitTopics.PAYMENT_REFUND_REQUESTED_ROUTING_KEY,
                    objectMapper.readValue(payload, PaymentRefundRequestedEventV1.class)
            );
            case "OrderRefundedEvent" -> rabbitTemplate.convertAndSend(
                    RabbitTopics.SAGA_EXCHANGE,
                    RabbitTopics.ORDER_REFUNDED_ROUTING_KEY,
                    objectMapper.readValue(payload, OrderRefundedEventV1.class)
            );
            case "OrderCompensationFailedEvent" -> rabbitTemplate.convertAndSend(
                    RabbitTopics.SAGA_EXCHANGE,
                    RabbitTopics.ORDER_COMPENSATION_FAILED_ROUTING_KEY,
                    objectMapper.readValue(payload, OrderCompensationFailedEventV1.class)
            );
            default -> throw new IllegalStateException("Unsupported outbox event type: " + eventType);
        }
    }
}
