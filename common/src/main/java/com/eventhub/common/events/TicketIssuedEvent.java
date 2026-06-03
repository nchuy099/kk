package com.eventhub.common.events;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TicketIssuedEvent(
        UUID orderId,
        String userId,
        List<String> ticketCodes,
        Instant occurredAt
) {
}

