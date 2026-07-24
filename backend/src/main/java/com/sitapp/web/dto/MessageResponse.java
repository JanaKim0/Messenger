package com.sitapp.web.dto;

import com.sitapp.domain.Message;
import com.sitapp.domain.MessageStatus;
import java.time.Instant;

public record MessageResponse(
        Long id,
        Long conversationId,
        Long senderId,
        String senderUsername,
        String content,
        MessageStatus status,
        Instant createdAt
) {
    public static MessageResponse from(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getConversation().getId(),
                message.getSender().getId(),
                message.getSender().getUsername(),
                message.getContent(),
                message.getStatus(),
                message.getCreatedAt()
        );
    }
}
