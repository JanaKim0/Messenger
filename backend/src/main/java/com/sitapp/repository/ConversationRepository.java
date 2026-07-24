package com.sitapp.repository;

import com.sitapp.domain.Conversation;
import com.sitapp.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    /**
     * Finds the conversation for a pair of users regardless of the order in
     * which they were stored.
     */
    @Query("""
            SELECT c FROM Conversation c
            WHERE (c.participantOne = :a AND c.participantTwo = :b)
               OR (c.participantOne = :b AND c.participantTwo = :a)
            """)
    Optional<Conversation> findByParticipants(@Param("a") User a, @Param("b") User b);

    /**
     * All conversations a user takes part in, most recently active first.
     */
    @Query("""
            SELECT c FROM Conversation c
            WHERE c.participantOne = :user OR c.participantTwo = :user
            ORDER BY c.lastMessageAt DESC
            """)
    List<Conversation> findAllForUser(@Param("user") User user);
}
