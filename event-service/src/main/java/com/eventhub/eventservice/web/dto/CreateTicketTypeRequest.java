package com.eventhub.eventservice.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateTicketTypeRequest(
        @NotBlank String name,
        @NotBlank String sectionName,
        @NotNull BigDecimal price,
        @NotBlank String currency,
        int totalQuantity
) {
}
