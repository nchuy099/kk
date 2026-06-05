package com.eventhub.orderservice.domain;

public enum OrderStatus {
    PENDING_PAYMENT,
    CONFIRMED,
    CANCELLED,
    EXPIRED,
    PAYMENT_FAILED
}
