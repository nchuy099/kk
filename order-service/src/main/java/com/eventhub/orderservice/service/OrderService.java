package com.eventhub.orderservice.service;

import com.eventhub.common.events.v1.OrderPaidEventV1;
import com.eventhub.common.events.v1.PaymentSucceededEventV1;
import com.eventhub.common.messaging.RabbitTopics;
import com.eventhub.orderservice.client.EventServiceClient;
import com.eventhub.orderservice.client.InventoryServiceClient;
import com.eventhub.orderservice.client.PaymentServiceClient;
import com.eventhub.orderservice.domain.Order;
import com.eventhub.orderservice.domain.OrderItem;
import com.eventhub.orderservice.domain.OrderOutboxEvent;
import com.eventhub.orderservice.domain.OrderStatus;
import com.eventhub.orderservice.repository.OrderRepository;
import com.eventhub.orderservice.repository.OrderOutboxEventRepository;
import com.eventhub.orderservice.service.exception.NotFoundException;
import com.eventhub.orderservice.service.exception.OrderConflictException;
import com.eventhub.orderservice.web.dto.CreateOrderRequest;
import com.eventhub.orderservice.web.dto.CreatePaymentRequest;
import com.eventhub.orderservice.web.dto.OrderItemResponse;
import com.eventhub.orderservice.web.dto.OrderResponse;
import com.eventhub.orderservice.web.dto.ReserveOrderRequest;
import java.math.BigDecimal;
import java.util.UUID;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.tracing.Tracer;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository orderRepository;
    private final EventServiceClient eventServiceClient;
    private final InventoryServiceClient inventoryServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final OrderOutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final Tracer tracer;

    public OrderService(
            OrderRepository orderRepository,
            EventServiceClient eventServiceClient,
            InventoryServiceClient inventoryServiceClient,
            PaymentServiceClient paymentServiceClient,
            OrderOutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper,
            Tracer tracer
    ) {
        this.orderRepository = orderRepository;
        this.eventServiceClient = eventServiceClient;
        this.inventoryServiceClient = inventoryServiceClient;
        this.paymentServiceClient = paymentServiceClient;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
        this.tracer = tracer;
    }

    @Transactional
    public OrderResponse createOrder(String userId, CreateOrderRequest request) {
        var orderId = UUID.randomUUID();
        var ticketType = eventServiceClient.getTicketType(request.ticketTypeId());
        var reservation = inventoryServiceClient.reserve(new ReserveOrderRequest(
                userId,
                request.ticketTypeId(),
                orderId,
                request.quantity()
        ));

        var unitPrice = ticketType.price();
        var totalAmount = unitPrice.multiply(BigDecimal.valueOf(request.quantity()));
        var order = Order.create(orderId, userId, reservation.id(), totalAmount, reservation.expiresAt());
        order.addItem(OrderItem.create(ticketType.id(), request.quantity(), unitPrice));
        order = orderRepository.save(order);

        var payment = paymentServiceClient.createPayment(new CreatePaymentRequest(order.getId(), totalAmount));
        order.setPaymentId(payment.paymentId());
        return toResponse(orderRepository.save(order));
    }

    public OrderResponse get(UUID id) {
        return toResponse(findOrder(id));
    }

    @Transactional
    public OrderResponse cancel(UUID id) {
        var order = findOrder(id);
        if (order.getStatus() == OrderStatus.PAID) {
            throw new OrderConflictException("Paid order cannot be cancelled");
        }
        inventoryServiceClient.release(order.getReservationId().toString());
        order.setStatus(OrderStatus.CANCELLED);
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public void handlePaymentSucceeded(PaymentSucceededEventV1 event) {
        var order = orderRepository.findById(event.orderId()).orElse(null);
        if (order == null || order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            return;
        }
        order.setStatus(OrderStatus.PAID);
        order.setPaymentId(event.paymentId());
        orderRepository.save(order);

        var item = order.getItems().get(0);
        createOrderPaidOutboxEvent(order, item, event.paymentId());
    }

    private Order findOrder(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id));
    }

    @Scheduled(fixedDelay = 30_000L)
    @Transactional
    public void expirePendingOrders() {
        var expiredOrders = orderRepository.findByStatusAndExpiresAtBefore(OrderStatus.PENDING_PAYMENT, java.time.Instant.now());
        for (var order : expiredOrders) {
            inventoryServiceClient.release(order.getReservationId().toString());
            order.setStatus(OrderStatus.EXPIRED);
            orderRepository.save(order);
        }
    }

    private OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getReservationId(),
                order.getPaymentId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getExpiresAt(),
                order.getItems().stream().map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getTicketTypeId(),
                        item.getQuantity(),
                        item.getUnitPrice()
                )).toList()
        );
    }

    private void createOrderPaidOutboxEvent(Order order, OrderItem item, UUID paymentId) {
        var event = new OrderPaidEventV1(
                UUID.randomUUID().toString(),
                "OrderPaidEvent",
                RabbitTopics.EVENT_VERSION_V1,
                currentCorrelationId(),
                java.time.Instant.now(),
                order.getId(),
                order.getReservationId(),
                paymentId,
                order.getUserId(),
                item.getTicketTypeId(),
                item.getQuantity(),
                order.getTotalAmount()
        );
        try {
            outboxEventRepository.save(OrderOutboxEvent.create(
                    "Order",
                    order.getId().toString(),
                    event.eventType(),
                    event.eventVersion(),
                    objectMapper.writeValueAsString(event)
            ));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize order outbox event", exception);
        }
    }

    private String currentCorrelationId() {
        var span = tracer.currentSpan();
        if (span != null && span.context() != null) {
            return span.context().traceId();
        }
        return UUID.randomUUID().toString();
    }
}
