package com.eventhub.orderservice.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateOrderItemRequest(
        @NotNull UUID ticketCategoryId,
        @Min(1) int quantity
) {
}
