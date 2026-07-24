package com.sitapp.service;

import com.sitapp.domain.User;
import com.sitapp.repository.UserRepository;
import com.sitapp.web.dto.ChangePasswordRequest;
import com.sitapp.web.dto.UpdateProfileRequest;
import com.sitapp.web.dto.UserResponse;
import com.sitapp.web.error.ApiException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Self-service operations for the authenticated user's own profile.
 */
@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        return UserResponse.from(load(userId));
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = load(userId);

        // If the email changed, make sure no other account already uses it.
        if (!user.getEmail().equalsIgnoreCase(request.email())
                && userRepository.existsByEmail(request.email())) {
            throw ApiException.conflict("Email is already registered");
        }

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhone(request.phone());
        user.setEmail(request.email());
        user.setPhoto(request.photo());
        return UserResponse.from(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = load(userId);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw ApiException.badRequest("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    }

    private User load(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User not found"));
    }
}
