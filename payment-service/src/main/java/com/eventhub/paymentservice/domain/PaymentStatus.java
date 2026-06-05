package com.eventhub.paymentservice.domain;

public enum PaymentStatus {
    PENDING,
    SUCCEEDED,
    FAILED,
    CANCELLED,
    REFUND_PENDING,
    REFUNDED,
    REFUND_FAILED
}
