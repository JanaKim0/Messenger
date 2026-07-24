package com.sitapp.web.dto;

/**
 * Sent over WebSocket to a message's sender when the recipient reads the
 * conversation, so their client can update delivery ticks to READ.
 */
public record ReadReceipt(
        Long conversationId,
        Long readerId
) {
}
