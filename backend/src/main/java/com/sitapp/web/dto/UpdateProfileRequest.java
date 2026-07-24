package com.sitapp.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Editable profile fields. Username is intentionally immutable.
 * {@code photo} is an optional base64 data URL.
 */
public record UpdateProfileRequest(
        @NotBlank @Size(max = 50) String firstName,
        @NotBlank @Size(max = 50) String lastName,
        @Size(max = 30) String phone,
        @NotBlank @Email @Size(max = 120) String email,
        String photo
) {
}
