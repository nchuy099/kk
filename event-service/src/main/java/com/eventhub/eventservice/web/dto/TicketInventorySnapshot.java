package com.eventhub.eventservice.web.dto;

import java.util.UUID;

public record TicketInventorySnapshot(
        UUID id,
        UUID ticketCategoryId,
        int totalQuantity,
        int availableQuantity,
        int reservedQuantity,
        int soldQuantity,
        long version
) {
}
