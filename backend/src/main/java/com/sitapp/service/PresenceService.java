package com.sitapp.service;

import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * Tracks which users currently have at least one active WebSocket session.
 * Used to decide whether a new message can be marked DELIVERED immediately.
 */
@Service
public class PresenceService {

    private final ConcurrentHashMap<String, Integer> sessionsByUser = new ConcurrentHashMap<>();

    public void connected(String username) {
        sessionsByUser.merge(username, 1, Integer::sum);
    }

    public void disconnected(String username) {
        sessionsByUser.computeIfPresent(username, (key, count) -> count <= 1 ? null : count - 1);
    }

    public boolean isOnline(String username) {
        return sessionsByUser.containsKey(username);
    }
}
