package com.eventhub.orderservice.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateOrderRequest(
        @NotBlank String userId,
        @NotNull UUID ticketTypeId,
        @Min(1) int quantity
) {
}

