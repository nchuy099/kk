package com.eventhub.userservice.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank String username,
        @Email String email,
        @NotBlank String password,
        String fullName,
        String phone
) {
}
