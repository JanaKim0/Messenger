package com.sitapp.domain;

/**
 * Registration/moderation status of a user account.
 * New accounts start as PENDING and must be approved by an administrator.
 */
public enum UserStatus {
    PENDING,
    APPROVED,
    REJECTED
}
