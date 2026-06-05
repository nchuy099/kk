package com.eventhub.common.messaging;

public final class RabbitTopics {
    private RabbitTopics() {
    }

    public static final String EVENT_VERSION_V1 = "v1";

    public static final String SAGA_EXCHANGE = "saga.exchange";
    public static final String SAGA_DLX = "saga.dlx";

    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";
    public static final String ORDER_PENDING_PAYMENT_ROUTING_KEY = "order.pending_payment";
    public static final String ORDER_CONFIRMED_ROUTING_KEY = "order.confirmed";
    public static final String ORDER_COMPLETED_ROUTING_KEY = "order.completed";
    public static final String ORDER_CANCELLED_ROUTING_KEY = "order.cancelled";
    public static final String ORDER_EXPIRED_ROUTING_KEY = "order.expired";
    public static final String ORDER_COMPENSATING_ROUTING_KEY = "order.compensating";
    public static final String ORDER_REFUNDED_ROUTING_KEY = "order.refunded";
    public static final String ORDER_COMPENSATION_FAILED_ROUTING_KEY = "order.compensation_failed";

    public static final String INVENTORY_RESERVED_ROUTING_KEY = "inventory.reserved";
    public static final String INVENTORY_RESERVE_FAILED_ROUTING_KEY = "inventory.reserve_failed";
    public static final String INVENTORY_CONFIRMED_ROUTING_KEY = "inventory.confirmed";
    public static final String INVENTORY_RELEASED_ROUTING_KEY = "inventory.released";
    public static final String INVENTORY_RELEASE_FAILED_ROUTING_KEY = "inventory.release_failed";
    public static final String RESERVATION_EXPIRED_ROUTING_KEY = "inventory.reservation_expired";

    public static final String PAYMENT_REQUESTED_ROUTING_KEY = "payment.requested";
    public static final String PAYMENT_CREATED_ROUTING_KEY = "payment.created";
    public static final String PAYMENT_SUCCEEDED_ROUTING_KEY = "payment.succeeded";
    public static final String PAYMENT_FAILED_ROUTING_KEY = "payment.failed";
    public static final String PAYMENT_CANCELLED_ROUTING_KEY = "payment.cancelled";
    public static final String PAYMENT_REFUND_REQUESTED_ROUTING_KEY = "payment.refund_requested";
    public static final String PAYMENT_REFUNDED_ROUTING_KEY = "payment.refunded";
    public static final String PAYMENT_REFUND_FAILED_ROUTING_KEY = "payment.refund_failed";

    public static final String TICKET_ISSUE_REQUESTED_ROUTING_KEY = "ticket.issue_requested";
    public static final String TICKET_ISSUED_ROUTING_KEY = "ticket.issued";
    public static final String TICKET_ISSUE_FAILED_ROUTING_KEY = "ticket.issue_failed";
    public static final String TICKET_CANCELLED_ROUTING_KEY = "ticket.cancelled";

    public static final String NOTIFICATION_BOOKING_COMPLETED_ROUTING_KEY = "notification.booking_completed";
    public static final String NOTIFICATION_BOOKING_CANCELLED_ROUTING_KEY = "notification.booking_cancelled";
    public static final String NOTIFICATION_BOOKING_REFUNDED_ROUTING_KEY = "notification.booking_refunded";
    public static final String NOTIFICATION_BOOKING_FAILED_ROUTING_KEY = "notification.booking_failed";

    public static final String INVENTORY_ORDER_CREATED_QUEUE = "inventory.saga.order-created";
    public static final String INVENTORY_ORDER_CREATED_DLQ = "inventory.saga.order-created.dlq";
    public static final String INVENTORY_ORDER_CONFIRMED_QUEUE = "inventory.saga.order-confirmed";
    public static final String INVENTORY_ORDER_CONFIRMED_DLQ = "inventory.saga.order-confirmed.dlq";
    public static final String INVENTORY_ORDER_CANCELLED_QUEUE = "inventory.saga.order-cancelled";
    public static final String INVENTORY_ORDER_CANCELLED_DLQ = "inventory.saga.order-cancelled.dlq";
    public static final String INVENTORY_ORDER_EXPIRED_QUEUE = "inventory.saga.order-expired";
    public static final String INVENTORY_ORDER_EXPIRED_DLQ = "inventory.saga.order-expired.dlq";
    public static final String INVENTORY_ORDER_REFUNDED_QUEUE = "inventory.saga.order-refunded";
    public static final String INVENTORY_ORDER_REFUNDED_DLQ = "inventory.saga.order-refunded.dlq";

    public static final String ORDER_INVENTORY_RESERVED_QUEUE = "order.saga.inventory-reserved";
    public static final String ORDER_INVENTORY_RESERVED_DLQ = "order.saga.inventory-reserved.dlq";
    public static final String ORDER_INVENTORY_RESERVE_FAILED_QUEUE = "order.saga.inventory-reserve-failed";
    public static final String ORDER_INVENTORY_RESERVE_FAILED_DLQ = "order.saga.inventory-reserve-failed.dlq";
    public static final String ORDER_PAYMENT_CREATED_QUEUE = "order.saga.payment-created";
    public static final String ORDER_PAYMENT_CREATED_DLQ = "order.saga.payment-created.dlq";
    public static final String ORDER_PAYMENT_SUCCEEDED_QUEUE = "order.saga.payment-succeeded";
    public static final String ORDER_PAYMENT_SUCCEEDED_DLQ = "order.saga.payment-succeeded.dlq";
    public static final String ORDER_PAYMENT_FAILED_QUEUE = "order.saga.payment-failed";
    public static final String ORDER_PAYMENT_FAILED_DLQ = "order.saga.payment-failed.dlq";
    public static final String ORDER_TICKET_ISSUED_QUEUE = "order.saga.ticket-issued";
    public static final String ORDER_TICKET_ISSUED_DLQ = "order.saga.ticket-issued.dlq";
    public static final String ORDER_TICKET_ISSUE_FAILED_QUEUE = "order.saga.ticket-issue-failed";
    public static final String ORDER_TICKET_ISSUE_FAILED_DLQ = "order.saga.ticket-issue-failed.dlq";
    public static final String ORDER_PAYMENT_REFUNDED_QUEUE = "order.saga.payment-refunded";
    public static final String ORDER_PAYMENT_REFUNDED_DLQ = "order.saga.payment-refunded.dlq";
    public static final String ORDER_PAYMENT_REFUND_FAILED_QUEUE = "order.saga.payment-refund-failed";
    public static final String ORDER_PAYMENT_REFUND_FAILED_DLQ = "order.saga.payment-refund-failed.dlq";
    public static final String ORDER_RESERVATION_EXPIRED_QUEUE = "order.saga.reservation-expired";
    public static final String ORDER_RESERVATION_EXPIRED_DLQ = "order.saga.reservation-expired.dlq";

    public static final String PAYMENT_PAYMENT_REQUESTED_QUEUE = "payment.saga.payment-requested";
    public static final String PAYMENT_PAYMENT_REQUESTED_DLQ = "payment.saga.payment-requested.dlq";
    public static final String PAYMENT_ORDER_EXPIRED_QUEUE = "payment.saga.order-expired";
    public static final String PAYMENT_ORDER_EXPIRED_DLQ = "payment.saga.order-expired.dlq";
    public static final String PAYMENT_REFUND_REQUESTED_QUEUE = "payment.saga.refund-requested";
    public static final String PAYMENT_REFUND_REQUESTED_DLQ = "payment.saga.refund-requested.dlq";

    public static final String TICKET_TICKET_ISSUE_REQUESTED_QUEUE = "ticket.saga.ticket-issue-requested";
    public static final String TICKET_TICKET_ISSUE_REQUESTED_DLQ = "ticket.saga.ticket-issue-requested.dlq";
    public static final String TICKET_ORDER_REFUNDED_QUEUE = "ticket.saga.order-refunded";
    public static final String TICKET_ORDER_REFUNDED_DLQ = "ticket.saga.order-refunded.dlq";
    public static final String TICKET_ORDER_CANCELLED_QUEUE = "ticket.saga.order-cancelled";
    public static final String TICKET_ORDER_CANCELLED_DLQ = "ticket.saga.order-cancelled.dlq";

    public static final String NOTIFICATION_ORDER_COMPLETED_QUEUE = "notification.saga.order-completed";
    public static final String NOTIFICATION_ORDER_COMPLETED_DLQ = "notification.saga.order-completed.dlq";
    public static final String NOTIFICATION_ORDER_CANCELLED_QUEUE = "notification.saga.order-cancelled";
    public static final String NOTIFICATION_ORDER_CANCELLED_DLQ = "notification.saga.order-cancelled.dlq";
    public static final String NOTIFICATION_ORDER_REFUNDED_QUEUE = "notification.saga.order-refunded";
    public static final String NOTIFICATION_ORDER_REFUNDED_DLQ = "notification.saga.order-refunded.dlq";
    public static final String NOTIFICATION_ORDER_COMPENSATION_FAILED_QUEUE = "notification.saga.order-compensation-failed";
    public static final String NOTIFICATION_ORDER_COMPENSATION_FAILED_DLQ = "notification.saga.order-compensation-failed.dlq";
}
