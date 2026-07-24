package com.sitapp.web.dto;

/**
 * Sent over WebSocket to a message's sender once the recipient comes online and
 * the message transitions from SENT to DELIVERED.
 */
public record DeliveredReceipt(
        Long conversationId,
        Long recipientId
) {
}
