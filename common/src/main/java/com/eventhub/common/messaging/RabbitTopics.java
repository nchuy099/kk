package com.eventhub.common.messaging;

public final class RabbitTopics {
    private RabbitTopics() {
    }

    public static final String EVENT_VERSION_V1 = "v1";

    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String TICKET_EXCHANGE = "ticket.exchange";

    public static final String PAYMENT_DLX = "payment.dlx";
    public static final String ORDER_DLX = "order.dlx";
    public static final String TICKET_DLX = "ticket.dlx";
    public static final String NOTIFICATION_DLX = "notification.dlx";

    public static final String PAYMENT_SUCCESS_ROUTING_KEY = "payment.succeeded";
    public static final String ORDER_CONFIRMED_ROUTING_KEY = "order.confirmed";
    public static final String TICKET_ISSUED_ROUTING_KEY = "ticket.issued";

    public static final String ORDER_PAYMENT_SUCCEEDED_QUEUE = "order.payment-succeeded";
    public static final String ORDER_PAYMENT_SUCCEEDED_DLQ = "order.payment-succeeded.dlq";
    public static final String INVENTORY_ORDER_CONFIRMED_QUEUE = "inventory.order-confirmed";
    public static final String INVENTORY_ORDER_CONFIRMED_DLQ = "inventory.order-confirmed.dlq";
    public static final String TICKET_ORDER_CONFIRMED_QUEUE = "ticket.order-confirmed";
    public static final String TICKET_ORDER_CONFIRMED_DLQ = "ticket.order-confirmed.dlq";
    public static final String NOTIFICATION_TICKET_ISSUED_QUEUE = "notification.ticket-issued";
    public static final String NOTIFICATION_TICKET_ISSUED_DLQ = "notification.ticket-issued.dlq";
}
