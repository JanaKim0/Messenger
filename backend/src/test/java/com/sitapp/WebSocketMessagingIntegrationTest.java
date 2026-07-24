package com.sitapp;

import static org.assertj.core.api.Assertions.assertThat;

import com.sitapp.domain.Conversation;
import com.sitapp.domain.MessageStatus;
import com.sitapp.domain.Role;
import com.sitapp.domain.User;
import com.sitapp.domain.UserStatus;
import com.sitapp.repository.ConversationRepository;
import com.sitapp.repository.UserRepository;
import com.sitapp.security.JwtService;
import com.sitapp.service.MessageService;
import com.sitapp.service.PresenceService;
import com.sitapp.web.dto.MessageResponse;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

/**
 * Verifies real-time delivery: a message sent by one user is pushed over
 * WebSocket to the recipient's personal queue, marked DELIVERED because the
 * recipient is connected.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:ws-it;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class WebSocketMessagingIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    JwtService jwtService;
    @Autowired
    ConversationRepository conversationRepository;
    @Autowired
    MessageService messageService;
    @Autowired
    PresenceService presenceService;

    @Test
    void recipientReceivesMessageInRealtime() throws Exception {
        User alice = createUser("alice_ws", "Alice");
        User bob = createUser("bob_ws", "Bob");

        Conversation conversation = new Conversation();
        conversation.setParticipantOne(alice);
        conversation.setParticipantTwo(bob);
        conversation.setLastMessageAt(Instant.now());
        conversationRepository.save(conversation);

        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new JacksonJsonMessageConverter());

        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + jwtService.generateToken(alice));

        StompSession session = stompClient
                .connectAsync("ws://localhost:" + port + "/ws", (WebSocketHttpHeaders) null, connectHeaders,
                        new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);

        BlockingQueue<MessageResponse> received = new LinkedBlockingQueue<>();
        session.subscribe("/user/queue/messages", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return MessageResponse.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                received.add((MessageResponse) payload);
            }
        });

        // Wait until the server has registered Alice's presence before Bob sends.
        waitUntilOnline("alice_ws");

        messageService.send(bob.getId(), conversation.getId(), "hello over websocket");

        MessageResponse delivered = received.poll(5, TimeUnit.SECONDS);
        assertThat(delivered).isNotNull();
        assertThat(delivered.content()).isEqualTo("hello over websocket");
        assertThat(delivered.senderUsername()).isEqualTo("bob_ws");
        assertThat(delivered.status()).isEqualTo(MessageStatus.DELIVERED);

        session.disconnect();
        stompClient.stop();
    }

    private void waitUntilOnline(String username) throws InterruptedException {
        for (int i = 0; i < 50 && !presenceService.isOnline(username); i++) {
            Thread.sleep(100);
        }
        assertThat(presenceService.isOnline(username)).isTrue();
    }

    private User createUser(String username, String firstName) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setFirstName(firstName);
        user.setLastName("Tester");
        user.setEmail(username + "@example.com");
        user.setRole(Role.USER);
        user.setStatus(UserStatus.APPROVED);
        return userRepository.save(user);
    }
}
