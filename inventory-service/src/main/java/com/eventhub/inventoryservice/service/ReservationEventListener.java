package com.eventhub.inventoryservice.service;

import com.eventhub.common.events.OrderPaidEvent;
import com.eventhub.common.messaging.RabbitTopics;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ReservationEventListener {
    private final InventoryService inventoryService;

    public ReservationEventListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @RabbitListener(queues = "inventory.order-paid")
    public void onOrderPaid(OrderPaidEvent event) {
        inventoryService.handleOrderPaidEvent(event);
    }
}

