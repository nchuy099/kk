package com.eventhub.orderservice.service;

import com.eventhub.common.events.v1.InventoryReserveFailedEventV1;
import com.eventhub.common.events.v1.InventoryReservedEventV1;
import com.eventhub.common.events.v1.OrderCancelledEventV1;
import com.eventhub.common.events.v1.OrderCompensatingEventV1;
import com.eventhub.common.events.v1.OrderCompletedEventV1;
import com.eventhub.common.events.v1.OrderCompensationFailedEventV1;
import com.eventhub.common.events.v1.OrderCreatedEventV1;
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
import com.eventhub.orderservice.client.EventServiceClient;
import com.eventhub.orderservice.domain.Order;
import com.eventhub.orderservice.domain.OrderItem;
import com.eventhub.orderservice.domain.OrderOutboxEvent;
import com.eventhub.orderservice.domain.OrderStatus;
import com.eventhub.orderservice.domain.ProcessedEvent;
import com.eventhub.orderservice.domain.SagaAuditEvent;
import com.eventhub.orderservice.repository.OrderOutboxEventRepository;
import com.eventhub.orderservice.repository.OrderRepository;
import com.eventhub.orderservice.repository.ProcessedEventRepository;
import com.eventhub.orderservice.repository.SagaAuditEventRepository;
import com.eventhub.orderservice.service.exception.NotFoundException;
import com.eventhub.orderservice.service.exception.OrderConflictException;
import com.eventhub.orderservice.web.dto.CreateOrderRequest;
import com.eventhub.orderservice.web.dto.OrderItemResponse;
import com.eventhub.orderservice.web.dto.OrderResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.tracing.Tracer;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrderService {
    private static final String SERVICE_NAME = "order-service";

    private final OrderRepository orderRepository;
    private final EventServiceClient eventServiceClient;
    private final OrderOutboxEventRepository outboxEventRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final SagaAuditEventRepository sagaAuditEventRepository;
    private final ObjectMapper objectMapper;
    private final Tracer tracer;
    private final Counter sagaStartedCounter;
    private final Counter sagaCompletedCounter;
    private final Counter sagaCancelledCounter;
    private final Counter sagaCompensatedCounter;
    private final Counter sagaFailedCounter;

    public OrderService(
            OrderRepository orderRepository,
            EventServiceClient eventServiceClient,
            OrderOutboxEventRepository outboxEventRepository,
            ProcessedEventRepository processedEventRepository,
            SagaAuditEventRepository sagaAuditEventRepository,
            ObjectMapper objectMapper,
            Tracer tracer,
            MeterRegistry meterRegistry
    ) {
        this.orderRepository = orderRepository;
        this.eventServiceClient = eventServiceClient;
        this.outboxEventRepository = outboxEventRepository;
        this.processedEventRepository = processedEventRepository;
        this.sagaAuditEventRepository = sagaAuditEventRepository;
        this.objectMapper = objectMapper;
        this.tracer = tracer;
        this.sagaStartedCounter = meterRegistry.counter("saga_started_total");
        this.sagaCompletedCounter = meterRegistry.counter("saga_completed_total");
        this.sagaCancelledCounter = meterRegistry.counter("saga_cancelled_total");
        this.sagaCompensatedCounter = meterRegistry.counter("saga_compensated_total");
        this.sagaFailedCounter = meterRegistry.counter("saga_failed_total");
    }

    @Transactional
    public OrderResponse createOrder(String userId, CreateOrderRequest request) {
        if (request.items().size() != 1) {
            throw new OrderConflictException("Only one ticket category per order is currently supported");
        }
        var itemRequest = request.items().get(0);
        var ticketCategory = eventServiceClient.getTicketType(itemRequest.ticketCategoryId());
        if (!ticketCategory.eventId().equals(request.eventId())) {
            throw new OrderConflictException("Ticket category does not belong to the selected event");
        }

        var orderId = UUID.randomUUID();
        var unitPrice = ticketCategory.price();
        var totalAmount = unitPrice.multiply(BigDecimal.valueOf(itemRequest.quantity()));
        var expiresAt = Instant.now().plusSeconds(600);

        var order = Order.create(orderId, userId, request.eventId(), null, totalAmount, ticketCategory.currency(), expiresAt);
        order.addItem(OrderItem.create(ticketCategory.id(), itemRequest.quantity(), unitPrice));
        orderRepository.save(order);

        createOutboxEvent(order.getId(), "OrderCreatedEvent", new OrderCreatedEventV1(
                UUID.randomUUID().toString(),
                "OrderCreatedEvent",
                RabbitTopics.EVENT_VERSION_V1,
                order.getId(),
                order.getId(),
                currentCorrelationId(),
                null,
                Instant.now(),
                null,
                order.getUserId(),
                order.getEventId(),
                itemRequest.ticketCategoryId(),
                itemRequest.quantity(),
                totalAmount,
                order.getCurrency(),
                expiresAt
        ));
        audit(order.getId(), order.getId(), "OrderCreatedEvent", "PENDING", null);
        sagaStartedCounter.increment();
        return toResponse(order);
    }

    public OrderResponse get(UUID id) {
        return toResponse(findOrder(id));
    }

    @Transactional
    public OrderResponse cancel(UUID id) {
        var order = findOrder(id);
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.REFUNDED) {
            throw new OrderConflictException("Completed or refunded order cannot be cancelled");
        }
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.EXPIRED) {
            return toResponse(order);
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        createOutboxEvent(order.getId(), "OrderCancelledEvent", new OrderCancelledEventV1(
                UUID.randomUUID().toString(),
                "OrderCancelledEvent",
                RabbitTopics.EVENT_VERSION_V1,
                order.getId(),
                order.getId(),
                currentCorrelationId(),
                null,
                Instant.now(),
                "Cancelled by user",
                order.getReservationId(),
                order.getPaymentId()
        ));
        audit(order.getId(), order.getId(), "OrderCancelledEvent", "CANCELLED", "Cancelled by user");
        sagaCancelledCounter.increment();
        return toResponse(order);
    }

    @Transactional
    public void handleInventoryReserved(InventoryReservedEventV1 event) {
        if (alreadyProcessed(event.eventId(), "inventoryReserved")) {
            return;
        }
        var order = findOrder(event.orderId());
        if (order.getStatus() != OrderStatus.PENDING) {
            markProcessed(event.eventId(), "inventoryReserved");
            return;
        }
        order.setReservationId(event.reservationId());
        order.setStatus(OrderStatus.INVENTORY_RESERVED);
        orderRepository.save(order);

        createOutboxEvent(order.getId(), "PaymentRequestedEvent", new PaymentRequestedEventV1(
                UUID.randomUUID().toString(),
                "PaymentRequestedEvent",
                RabbitTopics.EVENT_VERSION_V1,
                order.getId(),
                order.getId(),
                event.correlationId(),
                event.eventId(),
                Instant.now(),
                null,
                order.getUserId(),
                order.getTotalAmount(),
                order.getCurrency()
        ));
        audit(order.getId(), order.getId(), "InventoryReservedEvent", "INVENTORY_RESERVED", null);
        markProcessed(event.eventId(), "inventoryReserved");
    }

    @Transactional
    public void handleInventoryReserveFailed(InventoryReserveFailedEventV1 event) {
        if (alreadyProcessed(event.eventId(), "inventoryReserveFailed")) {
            return;
        }
        var order = findOrder(event.orderId());
        if (order.getStatus() == OrderStatus.CANCELLED) {
            markProcessed(event.eventId(), "inventoryReserveFailed");
            return;
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setFailureReason(event.reason());
        orderRepository.save(order);
        createOutboxEvent(order.getId(), "OrderCancelledEvent", new OrderCancelledEventV1(
                UUID.randomUUID().toString(),
                "OrderCancelledEvent",
                RabbitTopics.EVENT_VERSION_V1,
                order.getId(),
                order.getId(),
                event.correlationId(),
                event.eventId(),
                Instant.now(),
                event.reason(),
                null,
                null
        ));
        audit(order.getId(), order.getId(), "InventoryReserveFailedEvent", "CANCELLED", event.reason());
        markProcessed(event.eventId(), "inventoryReserveFailed");
        sagaCancelledCounter.increment();
    }

    @Transactional
    public void handlePaymentCreated(PaymentCreatedEventV1 event) {
        if (alreadyProcessed(event.eventId(), "paymentCreated")) {
            return;
        }
        var order = findOrder(event.orderId());
        if (order.getStatus() != OrderStatus.INVENTORY_RESERVED && order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            markProcessed(event.eventId(), "paymentCreated");
            return;
        }
        order.setPaymentId(event.paymentId());
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        orderRepository.save(order);
        audit(order.getId(), order.getId(), "PaymentCreatedEvent", "PENDING_PAYMENT", null);
        markProcessed(event.eventId(), "paymentCreated");
    }

    @Transactional
    public void handlePaymentSucceeded(PaymentSucceededEventV1 event) {
        if (alreadyProcessed(event.eventId(), "paymentSucceeded")) {
            return;
        }
        var order = findOrder(event.orderId());
        if (List.of(OrderStatus.PAID, OrderStatus.CONFIRMED, OrderStatus.COMPLETED, OrderStatus.REFUNDING, OrderStatus.REFUNDED).contains(order.getStatus())) {
            markProcessed(event.eventId(), "paymentSucceeded");
            return;
        }
        order.setStatus(OrderStatus.PAID);
        order.setPaymentId(event.paymentId());
        orderRepository.save(order);

        var item = order.getItems().get(0);
        createOutboxEvent(order.getId(), "OrderConfirmedEvent", new com.eventhub.common.events.v1.OrderConfirmedEventV1(
                UUID.randomUUID().toString(),
                "OrderConfirmedEvent",
                RabbitTopics.EVENT_VERSION_V1,
                order.getId(),
                order.getId(),
                event.correlationId(),
                event.eventId(),
                Instant.now(),
                null,
                order.getReservationId(),
                event.paymentId(),
                order.getUserId(),
                order.getEventId(),
                item.getTicketCategoryId(),
                item.getQuantity(),
                order.getTotalAmount(),
                order.getCurrency()
        ));
        createOutboxEvent(order.getId(), "TicketIssueRequestedEvent", new TicketIssueRequestedEventV1(
                UUID.randomUUID().toString(),
                "TicketIssueRequestedEvent",
                RabbitTopics.EVENT_VERSION_V1,
                order.getId(),
                order.getId(),
                event.correlationId(),
                event.eventId(),
                Instant.now(),
                null,
                order.getReservationId(),
                event.paymentId(),
                order.getUserId(),
                item.getTicketCategoryId(),
                item.getQuantity(),
                order.getTotalAmount(),
                order.getCurrency()
        ));
        audit(order.getId(), order.getId(), "PaymentSucceededEvent", "PAID", null);
        markProcessed(event.eventId(), "paymentSucceeded");
    }

    @Transactional
    public void handlePaymentFailed(PaymentFailedEventV1 event) {
        if (alreadyProcessed(event.eventId(), "paymentFailed")) {
            return;
        }
        var order = findOrder(event.orderId());
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.EXPIRED) {
            markProcessed(event.eventId(), "paymentFailed");
            return;
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setFailureReason(event.reason());
        orderRepository.save(order);
        createOutboxEvent(order.getId(), "OrderCancelledEvent", new OrderCancelledEventV1(
                UUID.randomUUID().toString(),
                "OrderCancelledEvent",
                RabbitTopics.EVENT_VERSION_V1,
                order.getId(),
                order.getId(),
                event.correlationId(),
                event.eventId(),
                Instant.now(),
                event.reason(),
                order.getReservationId(),
                order.getPaymentId()
        ));
        audit(order.getId(), order.getId(), "PaymentFailedEvent", "CANCELLED", event.reason());
        markProcessed(event.eventId(), "paymentFailed");
        sagaCancelledCounter.increment();
    }

    @Transactional
    public void handleTicketIssued(TicketIssuedEventV1 event) {
        if (alreadyProcessed(event.eventId(), "ticketIssued")) {
            return;
        }
        var order = findOrder(event.orderId());
        if (order.getStatus() == OrderStatus.COMPLETED) {
            markProcessed(event.eventId(), "ticketIssued");
            return;
        }
        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);
        createOutboxEvent(order.getId(), "OrderCompletedEvent", new OrderCompletedEventV1(
                UUID.randomUUID().toString(),
                "OrderCompletedEvent",
                RabbitTopics.EVENT_VERSION_V1,
                order.getId(),
                order.getId(),
                event.correlationId(),
                event.eventId(),
                Instant.now(),
                null,
                order.getUserId()
        ));
        audit(order.getId(), order.getId(), "TicketIssuedEvent", "COMPLETED", null);
        markProcessed(event.eventId(), "ticketIssued");
        sagaCompletedCounter.increment();
    }

    @Transactional
    public void handleTicketIssueFailed(TicketIssueFailedEventV1 event) {
        if (alreadyProcessed(event.eventId(), "ticketIssueFailed")) {
            return;
        }
        var order = findOrder(event.orderId());
        order.setStatus(OrderStatus.COMPENSATING);
        order.setFailureReason(event.reason());
        orderRepository.save(order);

        createOutboxEvent(order.getId(), "OrderCompensatingEvent", new OrderCompensatingEventV1(
                UUID.randomUUID().toString(),
                "OrderCompensatingEvent",
                RabbitTopics.EVENT_VERSION_V1,
                order.getId(),
                order.getId(),
                event.correlationId(),
                event.eventId(),
                Instant.now(),
                event.reason(),
                order.getPaymentId()
        ));
        createOutboxEvent(order.getId(), "PaymentRefundRequestedEvent", new PaymentRefundRequestedEventV1(
                UUID.randomUUID().toString(),
                "PaymentRefundRequestedEvent",
                RabbitTopics.EVENT_VERSION_V1,
                order.getId(),
                order.getId(),
                event.correlationId(),
                event.eventId(),
                Instant.now(),
                event.reason(),
                order.getPaymentId(),
                order.getTotalAmount(),
                order.getCurrency()
        ));
        audit(order.getId(), order.getId(), "TicketIssueFailedEvent", "COMPENSATING", event.reason());
        markProcessed(event.eventId(), "ticketIssueFailed");
    }

    @Transactional
    public void handlePaymentRefunded(PaymentRefundedEventV1 event) {
        if (alreadyProcessed(event.eventId(), "paymentRefunded")) {
            return;
        }
        var order = findOrder(event.orderId());
        order.setStatus(OrderStatus.REFUNDED);
        orderRepository.save(order);
        createOutboxEvent(order.getId(), "OrderRefundedEvent", new OrderRefundedEventV1(
                UUID.randomUUID().toString(),
                "OrderRefundedEvent",
                RabbitTopics.EVENT_VERSION_V1,
                order.getId(),
                order.getId(),
                event.correlationId(),
                event.eventId(),
                Instant.now(),
                event.reason(),
                event.paymentId(),
                order.getUserId()
        ));
        audit(order.getId(), order.getId(), "PaymentRefundedEvent", "REFUNDED", event.reason());
        markProcessed(event.eventId(), "paymentRefunded");
        sagaCompensatedCounter.increment();
    }

    @Transactional
    public void handlePaymentRefundFailed(PaymentRefundFailedEventV1 event) {
        if (alreadyProcessed(event.eventId(), "paymentRefundFailed")) {
            return;
        }
        var order = findOrder(event.orderId());
        order.setStatus(OrderStatus.COMPENSATION_FAILED);
        order.setFailureReason(event.reason());
        orderRepository.save(order);
        createOutboxEvent(order.getId(), "OrderCompensationFailedEvent", new OrderCompensationFailedEventV1(
                UUID.randomUUID().toString(),
                "OrderCompensationFailedEvent",
                RabbitTopics.EVENT_VERSION_V1,
                order.getId(),
                order.getId(),
                event.correlationId(),
                event.eventId(),
                Instant.now(),
                event.reason(),
                event.paymentId(),
                order.getUserId()
        ));
        audit(order.getId(), order.getId(), "PaymentRefundFailedEvent", "COMPENSATION_FAILED", event.reason());
        markProcessed(event.eventId(), "paymentRefundFailed");
        sagaFailedCounter.increment();
    }

    @Transactional
    public void handleReservationExpired(ReservationExpiredEventV1 event) {
        if (alreadyProcessed(event.eventId(), "reservationExpired")) {
            return;
        }
        var order = findOrder(event.orderId());
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.REFUNDED) {
            markProcessed(event.eventId(), "reservationExpired");
            return;
        }
        order.setStatus(OrderStatus.EXPIRED);
        order.setFailureReason(event.reason());
        orderRepository.save(order);
        createOutboxEvent(order.getId(), "OrderExpiredEvent", new OrderExpiredEventV1(
                UUID.randomUUID().toString(),
                "OrderExpiredEvent",
                RabbitTopics.EVENT_VERSION_V1,
                order.getId(),
                order.getId(),
                event.correlationId(),
                event.eventId(),
                Instant.now(),
                event.reason(),
                event.reservationId(),
                order.getPaymentId()
        ));
        audit(order.getId(), order.getId(), "ReservationExpiredEvent", "EXPIRED", event.reason());
        markProcessed(event.eventId(), "reservationExpired");
        sagaCancelledCounter.increment();
    }

    @Scheduled(fixedDelay = 30_000L)
    @Transactional
    public void expirePendingOrders() {
        var statuses = List.of(OrderStatus.PENDING, OrderStatus.PENDING_PAYMENT, OrderStatus.INVENTORY_RESERVED);
        var expiredOrders = orderRepository.findByStatusInAndExpiresAtBefore(statuses, Instant.now());
        for (var order : expiredOrders) {
            order.setStatus(OrderStatus.EXPIRED);
            order.setFailureReason("Order expired before completion");
            orderRepository.save(order);
            createOutboxEvent(order.getId(), "OrderExpiredEvent", new OrderExpiredEventV1(
                    UUID.randomUUID().toString(),
                    "OrderExpiredEvent",
                    RabbitTopics.EVENT_VERSION_V1,
                    order.getId(),
                    order.getId(),
                    currentCorrelationId(),
                    null,
                    Instant.now(),
                    "Order expired before completion",
                    order.getReservationId(),
                    order.getPaymentId()
            ));
            audit(order.getId(), order.getId(), "OrderExpiredEvent", "EXPIRED", "Order expired before completion");
        }
    }

    private boolean alreadyProcessed(String eventId, String consumerName) {
        return processedEventRepository.existsByEventKey(eventId + ":" + consumerName);
    }

    private void markProcessed(String eventId, String consumerName) {
        if (!alreadyProcessed(eventId, consumerName)) {
            processedEventRepository.save(ProcessedEvent.create(eventId, consumerName));
        }
    }

    private void audit(UUID sagaId, UUID orderId, String eventType, String status, String reason) {
        sagaAuditEventRepository.save(SagaAuditEvent.create(sagaId, orderId, eventType, SERVICE_NAME, status, reason));
    }

    private void createOutboxEvent(UUID aggregateId, String eventType, Object payload) {
        try {
            outboxEventRepository.save(OrderOutboxEvent.create(
                    "Order",
                    aggregateId.toString(),
                    eventType,
                    RabbitTopics.EVENT_VERSION_V1,
                    objectMapper.writeValueAsString(payload)
            ));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize order outbox event", exception);
        }
    }

    private Order findOrder(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id));
    }

    private OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getEventId(),
                order.getReservationId(),
                order.getPaymentId(),
                order.getTotalAmount(),
                order.getCurrency(),
                order.getStatus(),
                order.getExpiresAt(),
                order.getItems().stream().map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getTicketCategoryId(),
                        item.getQuantity(),
                        item.getUnitPrice()
                )).toList()
        );
    }

    private String currentCorrelationId() {
        var span = tracer.currentSpan();
        if (span != null && span.context() != null) {
            return span.context().traceId();
        }
        return UUID.randomUUID().toString();
    }
}
