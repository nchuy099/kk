package com.eventhub.ticketservice.web.dto;

import com.eventhub.ticketservice.domain.TicketStatus;
import java.time.Instant;
import java.util.UUID;

public record TicketResponse(
        UUID id,
        UUID orderId,
        String userId,
        UUID ticketCategoryId,
        String ticketCode,
        String qrCodePayload,
        TicketStatus status,
        Instant issuedAt
) {
}
