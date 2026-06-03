package com.eventhub.orderservice.service;

import com.eventhub.common.events.PaymentSucceededEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentSucceededListener {
    private final OrderService orderService;

    public PaymentSucceededListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @RabbitListener(queues = "order.payment-succeeded")
    public void onPaymentSucceeded(PaymentSucceededEvent event) {
        orderService.handlePaymentSucceeded(event);
    }
}

