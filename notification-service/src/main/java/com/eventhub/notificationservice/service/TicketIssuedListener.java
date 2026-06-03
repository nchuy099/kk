package com.eventhub.notificationservice.service;

import com.eventhub.common.events.TicketIssuedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TicketIssuedListener {
    private static final Logger log = LoggerFactory.getLogger(TicketIssuedListener.class);

    @RabbitListener(queues = "notification.ticket-issued")
    public void onTicketIssued(TicketIssuedEvent event) {
        log.info("Sending notification for order {} with tickets {}", event.orderId(), event.ticketCodes());
    }
}

