package com.sitapp.service;

import com.sitapp.domain.User;
import com.sitapp.domain.UserStatus;
import com.sitapp.repository.UserRepository;
import com.sitapp.web.dto.UserResponse;
import com.sitapp.web.error.ApiException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Administrative operations for moderating user registrations.
 */
@Service
public class AdminService {

    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Users still waiting for a moderation decision. */
    @Transactional(readOnly = true)
    public List<UserResponse> listPending() {
        return userRepository.findByStatus(UserStatus.PENDING).stream()
                .map(UserResponse::from)
                .toList();
    }

    /** All users, optionally filtered by status. */
    @Transactional(readOnly = true)
    public List<UserResponse> listUsers(UserStatus status) {
        List<User> users = (status == null)
                ? userRepository.findAll()
                : userRepository.findByStatus(status);
        return users.stream().map(UserResponse::from).toList();
    }

    /** Approves a pending (or previously rejected) registration. */
    @Transactional
    public UserResponse approve(Long userId) {
        User user = requirePendingDecision(userId);
        user.setStatus(UserStatus.APPROVED);
        return UserResponse.from(user);
    }

    /** Rejects a registration. */
    @Transactional
    public UserResponse reject(Long userId) {
        User user = requirePendingDecision(userId);
        user.setStatus(UserStatus.REJECTED);
        return UserResponse.from(user);
    }

    private User requirePendingDecision(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User not found"));
        if (user.getStatus() == UserStatus.APPROVED) {
            throw ApiException.conflict("User is already approved");
        }
        return user;
    }
}
