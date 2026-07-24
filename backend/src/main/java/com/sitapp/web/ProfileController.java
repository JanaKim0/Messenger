package com.sitapp.web;

import com.sitapp.security.UserPrincipal;
import com.sitapp.service.ProfileService;
import com.sitapp.web.dto.ChangePasswordRequest;
import com.sitapp.web.dto.UpdateProfileRequest;
import com.sitapp.web.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints for the authenticated user to manage their own profile.
 */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(profileService.getProfile(principal.getId()));
    }

    @PutMapping
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(principal.getId(), request));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        profileService.changePassword(principal.getId(), request);
        return ResponseEntity.noContent().build();
    }
}
