package com.sitapp.config;

import com.sitapp.service.MessageService;
import com.sitapp.service.PresenceService;
import java.security.Principal;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * Keeps {@link PresenceService} in sync with WebSocket session lifecycle and,
 * when a user connects, flushes their pending deliveries.
 */
@Component
public class WebSocketPresenceListener {

    private final PresenceService presenceService;
    private final MessageService messageService;

    public WebSocketPresenceListener(PresenceService presenceService, MessageService messageService) {
        this.presenceService = presenceService;
        this.messageService = messageService;
    }

    @EventListener
    public void onConnected(SessionConnectedEvent event) {
        String username = usernameOf(event.getUser());
        if (username != null) {
            presenceService.connected(username);
            messageService.markIncomingDelivered(username);
        }
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = usernameOf(accessor.getUser());
        if (username != null) {
            presenceService.disconnected(username);
        }
    }

    private String usernameOf(Principal principal) {
        return principal != null ? principal.getName() : null;
    }
}
