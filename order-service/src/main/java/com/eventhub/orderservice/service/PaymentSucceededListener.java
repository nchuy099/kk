package com.eventhub.orderservice.service;

import com.eventhub.common.events.v1.PaymentSucceededEventV1;
import com.eventhub.common.messaging.RabbitTopics;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentSucceededListener {
    private final OrderService orderService;

    public PaymentSucceededListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @RabbitListener(queues = RabbitTopics.ORDER_PAYMENT_SUCCEEDED_QUEUE)
    public void onPaymentSucceeded(PaymentSucceededEventV1 event) {
        orderService.handlePaymentSucceeded(event);
    }
}
