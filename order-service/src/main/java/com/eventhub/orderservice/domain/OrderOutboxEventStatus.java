package com.eventhub.orderservice.domain;

public enum OrderOutboxEventStatus {
    PENDING,
    SENT,
    FAILED
}
