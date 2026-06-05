package com.eventhub.common.events.v1;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderConfirmedEventV1(
        String eventId,
        String eventType,
        String eventVersion,
        String correlationId,
        Instant occurredAt,
        UUID orderId,
        UUID reservationId,
        UUID paymentId,
        String userId,
        UUID eventRefId,
        UUID ticketCategoryId,
        int quantity,
        BigDecimal totalAmount,
        String currency
) {
}
