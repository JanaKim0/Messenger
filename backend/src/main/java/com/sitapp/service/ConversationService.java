package com.sitapp.service;

import com.sitapp.domain.Conversation;
import com.sitapp.domain.Message;
import com.sitapp.domain.User;
import com.sitapp.domain.UserStatus;
import com.sitapp.repository.ConversationRepository;
import com.sitapp.repository.MessageRepository;
import com.sitapp.repository.UserRepository;
import com.sitapp.web.dto.ConversationResponse;
import com.sitapp.web.dto.UserSummary;
import com.sitapp.web.error.ApiException;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages private one-to-one conversations and the dialog list.
 */
@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public ConversationService(ConversationRepository conversationRepository,
                               MessageRepository messageRepository,
                               UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    /**
     * Returns the existing conversation between the caller and {@code otherUserId},
     * creating it if necessary.
     */
    @Transactional
    public ConversationResponse openWith(Long callerId, Long otherUserId) {
        if (callerId.equals(otherUserId)) {
            throw ApiException.badRequest("You cannot open a conversation with yourself");
        }
        User caller = requireUser(callerId);
        User other = requireUser(otherUserId);
        if (other.getStatus() != UserStatus.APPROVED) {
            throw ApiException.badRequest("This user is not available");
        }

        Conversation conversation = conversationRepository.findByParticipants(caller, other)
                .orElseGet(() -> createOrdered(caller, other));

        return toResponse(conversation, caller);
    }

    /** All of the caller's conversations, most recently active first. */
    @Transactional(readOnly = true)
    public List<ConversationResponse> listForUser(Long callerId) {
        User caller = requireUser(callerId);
        return conversationRepository.findAllForUser(caller).stream()
                .map(c -> toResponse(c, caller))
                .toList();
    }

    /** Creates a conversation with participants ordered by id for a stable unique key. */
    private Conversation createOrdered(User a, User b) {
        Conversation conversation = new Conversation();
        if (a.getId() <= b.getId()) {
            conversation.setParticipantOne(a);
            conversation.setParticipantTwo(b);
        } else {
            conversation.setParticipantOne(b);
            conversation.setParticipantTwo(a);
        }
        conversation.setLastMessageAt(Instant.now());
        return conversationRepository.save(conversation);
    }

    /** Maps a conversation to the caller's point of view. */
    private ConversationResponse toResponse(Conversation conversation, User caller) {
        User other = conversation.getParticipantOne().getId().equals(caller.getId())
                ? conversation.getParticipantTwo()
                : conversation.getParticipantOne();

        Message last = messageRepository.findFirstByConversationOrderByCreatedAtDesc(conversation);
        long unread = messageRepository.countUnread(conversation, caller);

        return new ConversationResponse(
                conversation.getId(),
                UserSummary.from(other),
                last != null ? last.getContent() : null,
                conversation.getLastMessageAt(),
                unread
        );
    }

    private User requireUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("User not found"));
    }
}
