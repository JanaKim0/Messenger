package com.sitapp.web;

import com.sitapp.security.UserPrincipal;
import com.sitapp.service.ConversationService;
import com.sitapp.web.dto.ConversationResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    /** List the caller's conversations, sorted by most recent activity. */
    @GetMapping
    public ResponseEntity<List<ConversationResponse>> list(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(conversationService.listForUser(principal.getId()));
    }

    /** Open (or create) a conversation with another user. */
    @PostMapping("/with/{userId}")
    public ResponseEntity<ConversationResponse> openWith(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long userId) {
        return ResponseEntity.ok(conversationService.openWith(principal.getId(), userId));
    }
}
