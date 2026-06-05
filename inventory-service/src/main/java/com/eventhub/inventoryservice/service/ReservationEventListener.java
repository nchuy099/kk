package com.eventhub.inventoryservice.service;

import com.eventhub.common.events.v1.OrderConfirmedEventV1;
import com.eventhub.common.messaging.RabbitTopics;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ReservationEventListener {
    private final InventoryService inventoryService;

    public ReservationEventListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @RabbitListener(queues = RabbitTopics.INVENTORY_ORDER_CONFIRMED_QUEUE)
    public void onOrderConfirmed(OrderConfirmedEventV1 event) {
        inventoryService.handleOrderConfirmedEvent(event);
    }
}
