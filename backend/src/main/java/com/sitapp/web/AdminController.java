package com.sitapp.web;

import com.sitapp.domain.UserStatus;
import com.sitapp.service.AdminService;
import com.sitapp.web.dto.UserResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Administrator-only endpoints for moderating registrations.
 * Access is restricted to ROLE_ADMIN in {@code SecurityConfig}.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /** List users awaiting approval. */
    @GetMapping("/users/pending")
    public ResponseEntity<List<UserResponse>> pending() {
        return ResponseEntity.ok(adminService.listPending());
    }

    /** List all users, optionally filtered by {@code ?status=PENDING|APPROVED|REJECTED}. */
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> users(@RequestParam(required = false) UserStatus status) {
        return ResponseEntity.ok(adminService.listUsers(status));
    }

    /** Approve a registration. */
    @PostMapping("/users/{id}/approve")
    public ResponseEntity<UserResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.approve(id));
    }

    /** Reject a registration. */
    @PostMapping("/users/{id}/reject")
    public ResponseEntity<UserResponse> reject(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.reject(id));
    }
}
