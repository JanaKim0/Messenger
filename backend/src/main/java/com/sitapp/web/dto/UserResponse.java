package com.sitapp.web.dto;

import com.sitapp.domain.Role;
import com.sitapp.domain.User;
import com.sitapp.domain.UserStatus;

/**
 * Public view of a user. Never exposes the password hash.
 */
public record UserResponse(
        Long id,
        String username,
        String firstName,
        String lastName,
        String email,
        String phone,
        String photo,
        Role role,
        UserStatus status
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getPhoto(),
                user.getRole(),
                user.getStatus()
        );
    }
}
