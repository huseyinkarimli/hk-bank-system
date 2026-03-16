package az.hkbank.module.ai.repository;

import az.hkbank.module.ai.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for ChatMessage entity operations.
 * Provides database access methods for chat message management.
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Finds all messages for a session ordered by creation date ascending.
     *
     * @param sessionId the session ID
     * @return list of chat messages
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.session.id = :sessionId ORDER BY m.createdAt ASC")
    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(@Param("sessionId") Long sessionId);

    /**
     * Finds the last 10 messages for a session ordered by creation date descending.
     *
     * @param sessionId the session ID
     * @return list of last 10 chat messages
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.session.id = :sessionId ORDER BY m.createdAt DESC LIMIT 10")
    List<ChatMessage> findTop10BySessionIdOrderByCreatedAtDesc(@Param("sessionId") Long sessionId);
}
