package com.eventhub.userservice.web.dto;

public record UpdateProfileRequest(
        String fullName,
        String phone
) {
}
