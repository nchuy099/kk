package com.eventhub.inventoryservice.web.dto;

import java.util.UUID;

public record TicketInventoryResponse(
        UUID id,
        UUID ticketTypeId,
        int totalQuantity,
        int availableQuantity,
        int reservedQuantity,
        int soldQuantity,
        long version
) {
}

