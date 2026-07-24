package com.sitapp.web;

import com.sitapp.security.UserPrincipal;
import com.sitapp.service.UserService;
import com.sitapp.web.dto.UserSummary;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** Search approved users by name or username: {@code /api/users/search?q=ali}. */
    @GetMapping("/search")
    public ResponseEntity<List<UserSummary>> search(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("q") String query) {
        return ResponseEntity.ok(userService.search(query, principal.getId()));
    }
}
