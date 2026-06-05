package com.eventhub.orderservice.service;

import com.eventhub.common.events.v1.InventoryReserveFailedEventV1;
import com.eventhub.common.events.v1.InventoryReservedEventV1;
import com.eventhub.common.events.v1.PaymentCreatedEventV1;
import com.eventhub.common.events.v1.PaymentFailedEventV1;
import com.eventhub.common.events.v1.PaymentRefundFailedEventV1;
import com.eventhub.common.events.v1.PaymentRefundedEventV1;
import com.eventhub.common.events.v1.PaymentSucceededEventV1;
import com.eventhub.common.events.v1.ReservationExpiredEventV1;
import com.eventhub.common.events.v1.TicketIssueFailedEventV1;
import com.eventhub.common.events.v1.TicketIssuedEventV1;
import com.eventhub.common.messaging.RabbitTopics;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SagaEventListener {
    private final OrderService orderService;

    @RabbitListener(queues = RabbitTopics.ORDER_INVENTORY_RESERVED_QUEUE)
    public void onInventoryReserved(InventoryReservedEventV1 event) {
        orderService.handleInventoryReserved(event);
    }

    @RabbitListener(queues = RabbitTopics.ORDER_INVENTORY_RESERVE_FAILED_QUEUE)
    public void onInventoryReserveFailed(InventoryReserveFailedEventV1 event) {
        orderService.handleInventoryReserveFailed(event);
    }

    @RabbitListener(queues = RabbitTopics.ORDER_PAYMENT_CREATED_QUEUE)
    public void onPaymentCreated(PaymentCreatedEventV1 event) {
        orderService.handlePaymentCreated(event);
    }

    @RabbitListener(queues = RabbitTopics.ORDER_PAYMENT_SUCCEEDED_QUEUE)
    public void onPaymentSucceeded(PaymentSucceededEventV1 event) {
        orderService.handlePaymentSucceeded(event);
    }

    @RabbitListener(queues = RabbitTopics.ORDER_PAYMENT_FAILED_QUEUE)
    public void onPaymentFailed(PaymentFailedEventV1 event) {
        orderService.handlePaymentFailed(event);
    }

    @RabbitListener(queues = RabbitTopics.ORDER_TICKET_ISSUED_QUEUE)
    public void onTicketIssued(TicketIssuedEventV1 event) {
        orderService.handleTicketIssued(event);
    }

    @RabbitListener(queues = RabbitTopics.ORDER_TICKET_ISSUE_FAILED_QUEUE)
    public void onTicketIssueFailed(TicketIssueFailedEventV1 event) {
        orderService.handleTicketIssueFailed(event);
    }

    @RabbitListener(queues = RabbitTopics.ORDER_PAYMENT_REFUNDED_QUEUE)
    public void onPaymentRefunded(PaymentRefundedEventV1 event) {
        orderService.handlePaymentRefunded(event);
    }

    @RabbitListener(queues = RabbitTopics.ORDER_PAYMENT_REFUND_FAILED_QUEUE)
    public void onPaymentRefundFailed(PaymentRefundFailedEventV1 event) {
        orderService.handlePaymentRefundFailed(event);
    }

    @RabbitListener(queues = RabbitTopics.ORDER_RESERVATION_EXPIRED_QUEUE)
    public void onReservationExpired(ReservationExpiredEventV1 event) {
        orderService.handleReservationExpired(event);
    }
}
