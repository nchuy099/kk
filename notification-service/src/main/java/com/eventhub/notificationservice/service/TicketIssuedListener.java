package com.eventhub.notificationservice.service;

import com.eventhub.common.events.v1.TicketIssuedEventV1;
import com.eventhub.common.messaging.RabbitTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TicketIssuedListener {
    private static final Logger log = LoggerFactory.getLogger(TicketIssuedListener.class);

    @RabbitListener(queues = RabbitTopics.NOTIFICATION_TICKET_ISSUED_QUEUE)
    public void onTicketIssued(TicketIssuedEventV1 event) {
        log.info("Sending notification for order {} with tickets {}", event.orderId(), event.ticketCodes());
    }
}
