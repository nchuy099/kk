package com.eventhub.eventservice.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateStadiumRequest(
        @NotBlank String name,
        @NotBlank String city,
        @NotBlank String country,
        @Min(1) int capacity,
        @NotBlank String address
) {
}
