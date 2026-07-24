package com.sitapp.web.dto;

import com.sitapp.domain.User;

/**
 * Minimal public view of another user (search results, conversation partner).
 * Deliberately omits email and phone.
 */
public record UserSummary(
        Long id,
        String username,
        String firstName,
        String lastName,
        String photo
) {
    public static UserSummary from(User user) {
        return new UserSummary(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoto()
        );
    }
}
