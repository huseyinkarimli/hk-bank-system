package az.hkbank.module.ai.service;

import az.hkbank.module.ai.dto.ChatMessageResponse;
import az.hkbank.module.ai.dto.ChatSessionResponse;

import java.util.List;

/**
 * Service interface for AI support operations.
 * Defines methods for managing chat sessions and messages.
 */
public interface AiSupportService {

    /**
     * Starts a new chat session for a user.
     *
     * @param userId the user ID
     * @return chat session response
     */
    ChatSessionResponse startSession(Long userId);

    /**
     * Sends a message in a chat session and gets AI response.
     *
     * @param userId the user ID
     * @param sessionId the session ID
     * @param message the user message
     * @return AI assistant response
     */
    ChatMessageResponse sendMessage(Long userId, String sessionId, String message);

    /**
     * Retrieves the message history for a session.
     *
     * @param userId the user ID
     * @param sessionId the session ID
     * @return list of chat messages
     */
    List<ChatMessageResponse> getSessionHistory(Long userId, String sessionId);

    /**
     * Retrieves all chat sessions for a user.
     *
     * @param userId the user ID
     * @return list of chat sessions
     */
    List<ChatSessionResponse> getUserSessions(Long userId);

    /**
     * Closes a chat session.
     *
     * @param userId the user ID
     * @param sessionId the session ID
     */
    void closeSession(Long userId, String sessionId);
}
