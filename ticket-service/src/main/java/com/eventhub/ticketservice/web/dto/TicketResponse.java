package com.eventhub.ticketservice.web.dto;

import com.eventhub.ticketservice.domain.TicketStatus;
import java.time.Instant;
import java.util.UUID;

public record TicketResponse(
        UUID id,
        UUID orderId,
        String userId,
        UUID ticketTypeId,
        String ticketCode,
        String qrCodeUrl,
        TicketStatus status,
        Instant issuedAt
) {
}

