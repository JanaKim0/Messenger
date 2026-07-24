package com.sitapp.domain;

/**
 * Delivery status of a message, mirroring WhatsApp-style ticks.
 * SENT      - stored on the server
 * DELIVERED - the recipient's client has received it
 * READ      - the recipient has opened the conversation
 */
public enum MessageStatus {
    SENT,
    DELIVERED,
    READ
}
