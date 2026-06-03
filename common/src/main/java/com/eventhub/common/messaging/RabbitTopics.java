package com.eventhub.common.messaging;

public final class RabbitTopics {
    private RabbitTopics() {
    }

    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String TICKET_EXCHANGE = "ticket.exchange";

    public static final String PAYMENT_SUCCESS_ROUTING_KEY = "payment.succeeded";
    public static final String ORDER_PAID_ROUTING_KEY = "order.paid";
    public static final String TICKET_ISSUED_ROUTING_KEY = "ticket.issued";
}

