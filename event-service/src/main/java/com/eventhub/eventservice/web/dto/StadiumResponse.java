package com.eventhub.eventservice.web.dto;

import java.util.UUID;

public record StadiumResponse(
        UUID id,
        String name,
        String city,
        String country,
        int capacity,
        String address
) {
}
