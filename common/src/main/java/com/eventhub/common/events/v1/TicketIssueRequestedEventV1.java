package com.eventhub.common.events.v1;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TicketIssueRequestedEventV1(
        String eventId,
        String eventType,
        String eventVersion,
        UUID sagaId,
        UUID orderId,
        String correlationId,
        String causationId,
        Instant occurredAt,
        String reason,
        UUID reservationId,
        UUID paymentId,
        String userId,
        UUID ticketCategoryId,
        int quantity,
        BigDecimal totalAmount,
        String currency
) {
}
