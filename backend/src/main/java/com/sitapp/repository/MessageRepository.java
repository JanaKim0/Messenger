package com.sitapp.repository;

import com.sitapp.domain.Conversation;
import com.sitapp.domain.Message;
import com.sitapp.domain.MessageStatus;
import com.sitapp.domain.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /** All messages in a conversation, oldest first. */
    List<Message> findByConversationOrderByCreatedAtAsc(Conversation conversation);

    /** The latest message in a conversation, if any. */
    Message findFirstByConversationOrderByCreatedAtDesc(Conversation conversation);

    /**
     * Number of messages in a conversation that the given user has not yet read
     * (i.e. sent by the other participant and not in READ status).
     */
    @Query("""
            SELECT COUNT(m) FROM Message m
            WHERE m.conversation = :conversation
              AND m.sender <> :user
              AND m.status <> com.sitapp.domain.MessageStatus.READ
            """)
    long countUnread(@Param("conversation") Conversation conversation, @Param("user") User user);

    /** Messages sent to the given user in a conversation with a specific status. */
    List<Message> findByConversationAndSenderNotAndStatus(
            Conversation conversation, User user, MessageStatus status);
}
