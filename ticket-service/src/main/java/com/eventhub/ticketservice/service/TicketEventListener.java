package com.eventhub.ticketservice.service;

import com.eventhub.common.events.v1.OrderConfirmedEventV1;
import com.eventhub.common.messaging.RabbitTopics;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TicketEventListener {
    private final TicketService ticketService;

    public TicketEventListener(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @RabbitListener(queues = RabbitTopics.TICKET_ORDER_CONFIRMED_QUEUE)
    public void onOrderConfirmed(OrderConfirmedEventV1 event) {
        ticketService.handleOrderConfirmed(event);
    }
}
