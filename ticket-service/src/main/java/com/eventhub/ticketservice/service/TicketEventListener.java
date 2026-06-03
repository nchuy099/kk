package com.eventhub.ticketservice.service;

import com.eventhub.common.events.OrderPaidEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TicketEventListener {
    private final TicketService ticketService;

    public TicketEventListener(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @RabbitListener(queues = "ticket.order-paid")
    public void onOrderPaid(OrderPaidEvent event) {
        ticketService.handleOrderPaid(event);
    }
}

