package com.eventhub.ticketservice.service;

import com.eventhub.common.events.v1.OrderCancelledEventV1;
import com.eventhub.common.events.v1.OrderRefundedEventV1;
import com.eventhub.common.events.v1.TicketIssueRequestedEventV1;
import com.eventhub.common.messaging.RabbitTopics;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SagaEventListener {
    private final TicketService ticketService;

    @RabbitListener(queues = RabbitTopics.TICKET_TICKET_ISSUE_REQUESTED_QUEUE)
    public void onTicketIssueRequested(TicketIssueRequestedEventV1 event) {
        ticketService.issueTickets(event);
    }

    @RabbitListener(queues = RabbitTopics.TICKET_ORDER_REFUNDED_QUEUE)
    public void onOrderRefunded(OrderRefundedEventV1 event) {
        ticketService.handleOrderRefunded(event);
    }

    @RabbitListener(queues = RabbitTopics.TICKET_ORDER_CANCELLED_QUEUE)
    public void onOrderCancelled(OrderCancelledEventV1 event) {
        ticketService.handleOrderCancelled(event);
    }
}
