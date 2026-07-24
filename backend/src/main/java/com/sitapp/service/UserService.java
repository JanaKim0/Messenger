package com.sitapp.service;

import com.sitapp.repository.UserRepository;
import com.sitapp.web.dto.UserSummary;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Searches approved users by username, first or last name. The caller is
     * excluded from the results. A blank term returns an empty list.
     */
    @Transactional(readOnly = true)
    public List<UserSummary> search(String term, Long callerId) {
        if (term == null || term.isBlank()) {
            return List.of();
        }
        return userRepository.searchApproved(term.trim(), callerId).stream()
                .map(UserSummary::from)
                .toList();
    }
}
