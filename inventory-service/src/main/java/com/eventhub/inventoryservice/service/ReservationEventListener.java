package com.eventhub.inventoryservice.service;

import com.eventhub.common.events.v1.OrderPaidEventV1;
import com.eventhub.common.messaging.RabbitTopics;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ReservationEventListener {
    private final InventoryService inventoryService;

    public ReservationEventListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @RabbitListener(queues = RabbitTopics.INVENTORY_ORDER_PAID_QUEUE)
    public void onOrderPaid(OrderPaidEventV1 event) {
        inventoryService.handleOrderPaidEvent(event);
    }
}
