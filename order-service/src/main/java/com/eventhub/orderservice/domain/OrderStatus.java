package com.eventhub.orderservice.domain;

public enum OrderStatus {
    PENDING,
    INVENTORY_RESERVED,
    PENDING_PAYMENT,
    PAID,
    CONFIRMED,
    COMPLETED,
    CANCELLED,
    EXPIRED,
    COMPENSATING,
    REFUNDING,
    REFUNDED,
    TICKET_ISSUE_FAILED,
    COMPENSATION_FAILED
}
