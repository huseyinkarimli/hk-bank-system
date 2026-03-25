package az.hkbank.module.ai.service.impl;

import az.hkbank.common.exception.BankException;
import az.hkbank.common.exception.ErrorCode;
import az.hkbank.module.ai.dto.ChatMessageResponse;
import az.hkbank.module.ai.dto.ChatSessionResponse;
import az.hkbank.module.ai.entity.ChatMessage;
import az.hkbank.module.ai.entity.ChatSession;
import az.hkbank.module.ai.entity.MessageRole;
import az.hkbank.module.ai.mapper.ChatMapper;
import az.hkbank.module.ai.repository.ChatMessageRepository;
import az.hkbank.module.ai.repository.ChatSessionRepository;
import az.hkbank.module.ai.service.AiSupportService;
import az.hkbank.module.ai.service.GeminiApiService;
import az.hkbank.module.user.entity.User;
import az.hkbank.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of AiSupportService interface.
 * Handles AI chat session management and message processing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiSupportServiceImpl implements AiSupportService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final GeminiApiService geminiApiService;
    private final ChatMapper chatMapper;

    @Override
    @Transactional
    public ChatSessionResponse startSession(Long userId) {
        log.info("Starting new chat session for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BankException(ErrorCode.USER_NOT_FOUND));

        ChatSession session = ChatSession.builder()
                .user(user)
                .title("Yeni söhbət")
                .isActive(true)
                .build();

        ChatSession savedSession = chatSessionRepository.save(session);

        log.info("Chat session created: {}", savedSession.getSessionId());

        ChatSessionResponse response = chatMapper.toChatSessionResponse(savedSession);
        response.setMessageCount(0);

        return response;
    }

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(Long userId, String sessionId, String message) {
        log.info("Processing message for session: {}", sessionId);

        ChatSession session = chatSessionRepository.findBySessionIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new BankException(ErrorCode.CHAT_SESSION_NOT_FOUND));

        if (!session.isActive()) {
            throw new BankException(ErrorCode.CHAT_SESSION_NOT_FOUND, "Session is closed");
        }

        ChatMessage userMessage = ChatMessage.builder()
                .session(session)
                .role(MessageRole.USER)
                .content(message)
                .build();

        chatMessageRepository.save(userMessage);

        if (session.getTitle().equals("Yeni söhbət")) {
            String title = message.length() > 50 ? message.substring(0, 47) + "..." : message;
            session.setTitle(title);
        }

        List<ChatMessage> history = chatMessageRepository.findTop10BySessionIdOrderByCreatedAtDesc(session.getId());

        String aiResponse = geminiApiService.sendMessage(history, message);

        ChatMessage assistantMessage = ChatMessage.builder()
                .session(session)
                .role(MessageRole.ASSISTANT)
                .content(aiResponse)
                .build();

        ChatMessage savedAssistantMessage = chatMessageRepository.save(assistantMessage);

        session.setUpdatedAt(java.time.LocalDateTime.now());
        chatSessionRepository.save(session);

        log.info("AI response generated for session: {}", sessionId);

        return chatMapper.toChatMessageResponse(savedAssistantMessage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getSessionHistory(Long userId, String sessionId) {
        log.info("Fetching session history: {} for user: {}", sessionId, userId);

        ChatSession session = chatSessionRepository.findBySessionIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new BankException(ErrorCode.CHAT_SESSION_NOT_FOUND));

        List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId());

        return messages.stream()
                .map(chatMapper::toChatMessageResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatSessionResponse> getUserSessions(Long userId) {
        log.info("Fetching all sessions for user: {}", userId);

        List<ChatSession> sessions = chatSessionRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return sessions.stream()
                .map(session -> {
                    ChatSessionResponse response = chatMapper.toChatSessionResponse(session);
                    int messageCount = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId()).size();
                    response.setMessageCount(messageCount);
                    return response;
                })
                .toList();
    }

    @Override
    @Transactional
    public void closeSession(Long userId, String sessionId) {
        log.info("Closing session: {} for user: {}", sessionId, userId);

        ChatSession session = chatSessionRepository.findBySessionIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new BankException(ErrorCode.CHAT_SESSION_NOT_FOUND));

        session.setActive(false);
        chatSessionRepository.save(session);

        log.info("Session closed: {}", sessionId);
    }
}
