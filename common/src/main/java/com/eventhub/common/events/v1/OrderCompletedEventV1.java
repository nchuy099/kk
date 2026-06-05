package com.eventhub.common.events.v1;

import java.time.Instant;
import java.util.UUID;

public record OrderCompletedEventV1(
        String eventId,
        String eventType,
        String eventVersion,
        UUID sagaId,
        UUID orderId,
        String correlationId,
        String causationId,
        Instant occurredAt,
        String reason,
        String userId
) {
}
