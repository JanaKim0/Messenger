package com.sitapp.web.dto;

/**
 * Returned on successful login: a JWT access token plus the authenticated user.
 */
public record AuthResponse(
        String token,
        String tokenType,
        UserResponse user
) {
    public static AuthResponse of(String token, UserResponse user) {
        return new AuthResponse(token, "Bearer", user);
    }
}
