package com.eventhub.inventoryservice.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ReserveRequest(
        @NotBlank String userId,
        @NotNull UUID ticketTypeId,
        @NotNull UUID orderId,
        @Min(1) int quantity
) {
}

