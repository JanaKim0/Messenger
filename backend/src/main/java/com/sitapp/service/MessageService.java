package com.sitapp.service;

import com.sitapp.domain.Conversation;
import com.sitapp.domain.Message;
import com.sitapp.domain.MessageStatus;
import com.sitapp.domain.User;
import com.sitapp.repository.ConversationRepository;
import com.sitapp.repository.MessageRepository;
import com.sitapp.repository.UserRepository;
import com.sitapp.web.dto.DeliveredReceipt;
import com.sitapp.web.dto.MessageResponse;
import com.sitapp.web.dto.ReadReceipt;
import com.sitapp.web.error.ApiException;
import java.time.Instant;
import java.util.List;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sends and reads text messages, maintaining WhatsApp-style delivery statuses
 * and pushing real-time updates over WebSocket.
 */
@Service
public class MessageService {

    /** STOMP destinations the recipient/sender subscribe to (prefixed with /user). */
    private static final String QUEUE_MESSAGES = "/queue/messages";
    private static final String QUEUE_READ = "/queue/read";
    private static final String QUEUE_DELIVERED = "/queue/delivered";

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final PresenceService presenceService;

    public MessageService(ConversationRepository conversationRepository,
                          MessageRepository messageRepository,
                          UserRepository userRepository,
                          SimpMessagingTemplate messagingTemplate,
                          PresenceService presenceService) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.presenceService = presenceService;
    }

    @Transactional
    public MessageResponse send(Long senderId, Long conversationId, String content) {
        User sender = requireUser(senderId);
        Conversation conversation = requireConversation(conversationId);
        User recipient = otherParticipant(conversation, sender);

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(content);
        // If the recipient is currently connected, the message is delivered right away.
        message.setStatus(presenceService.isOnline(recipient.getUsername())
                ? MessageStatus.DELIVERED
                : MessageStatus.SENT);
        messageRepository.save(message);

        conversation.setLastMessageAt(Instant.now());

        MessageResponse response = MessageResponse.from(message);
        // Push the new message to the recipient in real time.
        messagingTemplate.convertAndSendToUser(recipient.getUsername(), QUEUE_MESSAGES, response);
        return response;
    }

    /**
     * Returns all messages in a conversation, oldest first, and marks messages
     * addressed to the caller as READ (notifying the sender over WebSocket).
     */
    @Transactional
    public List<MessageResponse> listAndMarkRead(Long userId, Long conversationId) {
        User user = requireUser(userId);
        Conversation conversation = requireConversation(conversationId);
        User other = otherParticipant(conversation, user);

        List<Message> unread = messageRepository
                .findByConversationAndSenderNotAndStatusNot(conversation, user, MessageStatus.READ);
        // DELIVERED and SENT incoming messages both become READ.
        boolean anyRead = false;
        for (Message m : unread) {
            m.setStatus(MessageStatus.READ);
            anyRead = true;
        }
        if (anyRead) {
            messagingTemplate.convertAndSendToUser(
                    other.getUsername(), QUEUE_READ, new ReadReceipt(conversationId, userId));
        }

        return messageRepository.findByConversationOrderByCreatedAtAsc(conversation).stream()
                .map(MessageResponse::from)
                .toList();
    }

    /**
     * When a user comes online, upgrades all messages addressed to them from
     * SENT to DELIVERED and notifies each sender over WebSocket.
     */
    @Transactional
    public void markIncomingDelivered(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return;
        }
        List<Message> pending = messageRepository.findUndeliveredForRecipient(user);
        for (Message m : pending) {
            m.setStatus(MessageStatus.DELIVERED);
            messagingTemplate.convertAndSendToUser(
                    m.getSender().getUsername(), QUEUE_DELIVERED,
                    new DeliveredReceipt(m.getConversation().getId(), user.getId()));
        }
    }

    private User otherParticipant(Conversation conversation, User user) {
        boolean isParticipant = conversation.getParticipantOne().getId().equals(user.getId())
                || conversation.getParticipantTwo().getId().equals(user.getId());
        if (!isParticipant) {
            throw ApiException.forbidden("You are not part of this conversation");
        }
        return conversation.getParticipantOne().getId().equals(user.getId())
                ? conversation.getParticipantTwo()
                : conversation.getParticipantOne();
    }

    private User requireUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("User not found"));
    }

    private Conversation requireConversation(Long id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Conversation not found"));
    }
}
