package com.eventhub.common.events.v1;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentCreatedEventV1(
        String eventId,
        String eventType,
        String eventVersion,
        UUID sagaId,
        UUID orderId,
        String correlationId,
        String causationId,
        Instant occurredAt,
        String reason,
        UUID paymentId,
        BigDecimal amount,
        String currency,
        String provider
) {
}
