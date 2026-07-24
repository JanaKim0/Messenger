package com.sitapp.web;

import com.sitapp.security.UserPrincipal;
import com.sitapp.service.MessageService;
import com.sitapp.web.dto.MessageResponse;
import com.sitapp.web.dto.SendMessageRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conversations/{conversationId}/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /** Fetch the conversation history and mark incoming messages as read. */
    @GetMapping
    public ResponseEntity<List<MessageResponse>> history(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long conversationId) {
        return ResponseEntity.ok(messageService.listAndMarkRead(principal.getId(), conversationId));
    }

    /** Send a text message. Delivered to the recipient in real time over WebSocket. */
    @PostMapping
    public ResponseEntity<MessageResponse> send(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long conversationId,
            @Valid @RequestBody SendMessageRequest request) {
        MessageResponse response = messageService.send(principal.getId(), conversationId, request.content());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
