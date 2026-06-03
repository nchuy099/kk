package com.eventhub.common.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentSucceededEvent(
        UUID paymentId,
        UUID orderId,
        String transactionId,
        BigDecimal amount,
        Instant occurredAt
) {
}

