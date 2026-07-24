package com.sitapp.web.dto;

import java.time.Instant;

/**
 * A conversation as seen by one participant: the other user, a preview of the
 * last message, when it happened, and how many messages are still unread.
 */
public record ConversationResponse(
        Long id,
        UserSummary otherUser,
        String lastMessage,
        Instant lastMessageAt,
        long unreadCount
) {
}
