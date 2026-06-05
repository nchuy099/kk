package com.eventhub.userservice.web.dto;

public record AuthUser(
        String id,
        String username,
        String email,
        String roles
) {
}
