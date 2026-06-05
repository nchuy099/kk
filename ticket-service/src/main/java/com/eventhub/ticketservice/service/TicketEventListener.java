package com.eventhub.ticketservice.service;

import com.eventhub.common.events.v1.OrderPaidEventV1;
import com.eventhub.common.messaging.RabbitTopics;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TicketEventListener {
    private final TicketService ticketService;

    public TicketEventListener(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @RabbitListener(queues = RabbitTopics.TICKET_ORDER_PAID_QUEUE)
    public void onOrderPaid(OrderPaidEventV1 event) {
        ticketService.handleOrderPaid(event);
    }
}
