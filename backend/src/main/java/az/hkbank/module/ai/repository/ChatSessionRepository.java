package az.hkbank.module.ai.repository;

import az.hkbank.module.ai.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ChatSession entity operations.
 * Provides database access methods for chat session management.
 */
@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    /**
     * Finds all chat sessions for a user ordered by creation date descending.
     *
     * @param userId the user ID
     * @return list of chat sessions
     */
    List<ChatSession> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Finds a chat session by session ID and user ID.
     *
     * @param sessionId the session ID
     * @param userId the user ID
     * @return Optional containing the chat session if found
     */
    Optional<ChatSession> findBySessionIdAndUserId(String sessionId, Long userId);

    /**
     * Finds all active chat sessions for a user.
     *
     * @param userId the user ID
     * @return list of active chat sessions
     */
    List<ChatSession> findByUserIdAndIsActiveTrue(Long userId);
}
