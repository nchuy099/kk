package com.eventhub.common.events.v1;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentSucceededEventV1(
        String eventId,
        String eventType,
        String eventVersion,
        String correlationId,
        Instant occurredAt,
        UUID paymentId,
        UUID orderId,
        String transactionId,
        BigDecimal amount
) {
}
