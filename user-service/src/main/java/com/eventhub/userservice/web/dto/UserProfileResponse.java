package com.eventhub.userservice.web.dto;

import com.eventhub.userservice.domain.UserStatus;
import java.time.Instant;

public record UserProfileResponse(
        String id,
        String username,
        String email,
        String fullName,
        String phone,
        UserStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
