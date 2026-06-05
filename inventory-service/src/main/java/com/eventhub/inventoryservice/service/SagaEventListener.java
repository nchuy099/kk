package com.eventhub.inventoryservice.service;

import com.eventhub.common.events.v1.OrderCancelledEventV1;
import com.eventhub.common.events.v1.OrderConfirmedEventV1;
import com.eventhub.common.events.v1.OrderCreatedEventV1;
import com.eventhub.common.events.v1.OrderExpiredEventV1;
import com.eventhub.common.events.v1.OrderRefundedEventV1;
import com.eventhub.common.messaging.RabbitTopics;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SagaEventListener {
    private final InventoryService inventoryService;

    public SagaEventListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @RabbitListener(queues = RabbitTopics.INVENTORY_ORDER_CREATED_QUEUE)
    public void onOrderCreated(OrderCreatedEventV1 event) {
        inventoryService.handleOrderCreated(event);
    }

    @RabbitListener(queues = RabbitTopics.INVENTORY_ORDER_CONFIRMED_QUEUE)
    public void onOrderConfirmed(OrderConfirmedEventV1 event) {
        inventoryService.handleOrderConfirmedEvent(event);
    }

    @RabbitListener(queues = RabbitTopics.INVENTORY_ORDER_CANCELLED_QUEUE)
    public void onOrderCancelled(OrderCancelledEventV1 event) {
        inventoryService.handleOrderCancelled(event);
    }

    @RabbitListener(queues = RabbitTopics.INVENTORY_ORDER_EXPIRED_QUEUE)
    public void onOrderExpired(OrderExpiredEventV1 event) {
        inventoryService.handleOrderExpired(event);
    }

    @RabbitListener(queues = RabbitTopics.INVENTORY_ORDER_REFUNDED_QUEUE)
    public void onOrderRefunded(OrderRefundedEventV1 event) {
        inventoryService.handleOrderRefunded(event);
    }
}
