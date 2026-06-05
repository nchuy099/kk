package com.eventhub.paymentservice.service;

import com.eventhub.common.events.v1.OrderExpiredEventV1;
import com.eventhub.common.events.v1.PaymentRefundRequestedEventV1;
import com.eventhub.common.events.v1.PaymentRequestedEventV1;
import com.eventhub.common.messaging.RabbitTopics;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SagaEventListener {
    private final PaymentService paymentService;

    @RabbitListener(queues = RabbitTopics.PAYMENT_PAYMENT_REQUESTED_QUEUE)
    public void onPaymentRequested(PaymentRequestedEventV1 event) {
        paymentService.handlePaymentRequested(event);
    }

    @RabbitListener(queues = RabbitTopics.PAYMENT_REFUND_REQUESTED_QUEUE)
    public void onRefundRequested(PaymentRefundRequestedEventV1 event) {
        paymentService.handleRefundRequested(event);
    }

    @RabbitListener(queues = RabbitTopics.PAYMENT_ORDER_EXPIRED_QUEUE)
    public void onOrderExpired(OrderExpiredEventV1 event) {
        paymentService.handleOrderExpired(event);
    }
}
