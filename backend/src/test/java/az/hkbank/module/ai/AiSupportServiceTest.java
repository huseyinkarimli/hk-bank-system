package az.hkbank.module.ai;

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
import az.hkbank.module.ai.service.GeminiApiService;
import az.hkbank.module.ai.service.impl.AiSupportServiceImpl;
import az.hkbank.module.user.entity.Role;
import az.hkbank.module.user.entity.User;
import az.hkbank.module.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AiSupportServiceImpl.
 * Tests AI chat session and message operations.
 */
@ExtendWith(MockitoExtension.class)
class AiSupportServiceTest {

    @Mock
    private ChatSessionRepository chatSessionRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GeminiApiService geminiApiService;

    @Mock
    private ChatMapper chatMapper;

    @InjectMocks
    private AiSupportServiceImpl aiSupportService;

    private User user;
    private ChatSession session;
    private ChatMessage userMessage;
    private ChatMessage assistantMessage;
    private ChatSessionResponse sessionResponse;
    private ChatMessageResponse messageResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .firstName("Huseyin")
                .lastName("Karimli")
                .email("huseyin.karimli@hkbank.az")
                .password("$2a$10$encodedPassword")
                .phoneNumber("+994501234567")
                .role(Role.USER)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        String sessionId = UUID.randomUUID().toString();

        session = ChatSession.builder()
                .id(1L)
                .user(user)
                .sessionId(sessionId)
                .title("Yeni söhbət")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userMessage = ChatMessage.builder()
                .id(1L)
                .session(session)
                .role(MessageRole.USER)
                .content("Hesab necə açılır?")
                .createdAt(LocalDateTime.now())
                .build();

        assistantMessage = ChatMessage.builder()
                .id(2L)
                .session(session)
                .role(MessageRole.ASSISTANT)
                .content("Hesab açmaq üçün mobil tətbiqdən qeydiyyatdan keçin.")
                .createdAt(LocalDateTime.now())
                .build();

        sessionResponse = ChatSessionResponse.builder()
                .sessionId(sessionId)
                .title("Yeni söhbət")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .messageCount(0)
                .build();

        messageResponse = ChatMessageResponse.builder()
                .id(2L)
                .role(MessageRole.ASSISTANT)
                .content("Hesab açmaq üçün mobil tətbiqdən qeydiyyatdan keçin.")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void startSession_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(chatSessionRepository.save(any(ChatSession.class))).thenReturn(session);
        when(chatMapper.toChatSessionResponse(any(ChatSession.class))).thenReturn(sessionResponse);

        ChatSessionResponse response = aiSupportService.startSession(1L);

        assertNotNull(response);
        assertEquals("Yeni söhbət", response.getTitle());
        assertTrue(response.isActive());
        assertEquals(0, response.getMessageCount());

        verify(userRepository).findById(1L);
        verify(chatSessionRepository).save(argThat(s ->
                s.getUser().getId().equals(1L) &&
                s.getTitle().equals("Yeni söhbət") &&
                s.isActive()
        ));
    }

    @Test
    void startSession_UserNotFound_ThrowsBankException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        BankException exception = assertThrows(BankException.class, () -> {
            aiSupportService.startSession(1L);
        });

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(userRepository).findById(1L);
        verify(chatSessionRepository, never()).save(any(ChatSession.class));
    }

    @Test
    void sendMessage_Success() {
        when(chatSessionRepository.findBySessionIdAndUserId(session.getSessionId(), 1L))
                .thenReturn(Optional.of(session));
        when(chatMessageRepository.save(any(ChatMessage.class)))
                .thenReturn(userMessage)
                .thenReturn(assistantMessage);
        when(chatMessageRepository.findTop10BySessionIdOrderByCreatedAtDesc(1L))
                .thenReturn(Arrays.asList());
        when(geminiApiService.sendMessage(anyList(), anyString()))
                .thenReturn("Hesab açmaq üçün mobil tətbiqdən qeydiyyatdan keçin.");
        when(chatMapper.toChatMessageResponse(any(ChatMessage.class))).thenReturn(messageResponse);

        ChatMessageResponse response = aiSupportService.sendMessage(
                1L,
                session.getSessionId(),
                "Hesab necə açılır?"
        );

        assertNotNull(response);
        assertEquals(MessageRole.ASSISTANT, response.getRole());
        assertNotNull(response.getContent());

        verify(chatSessionRepository).findBySessionIdAndUserId(session.getSessionId(), 1L);
        verify(chatMessageRepository, times(2)).save(any(ChatMessage.class));
        verify(geminiApiService).sendMessage(anyList(), eq("Hesab necə açılır?"));
    }

    @Test
    void sendMessage_SessionNotFound_ThrowsBankException() {
        when(chatSessionRepository.findBySessionIdAndUserId(anyString(), eq(1L)))
                .thenReturn(Optional.empty());

        BankException exception = assertThrows(BankException.class, () -> {
            aiSupportService.sendMessage(1L, "invalid-session-id", "Test message");
        });

        assertEquals(ErrorCode.CHAT_SESSION_NOT_FOUND, exception.getErrorCode());
        verify(chatSessionRepository).findBySessionIdAndUserId("invalid-session-id", 1L);
        verify(chatMessageRepository, never()).save(any(ChatMessage.class));
    }

    @Test
    void sendMessage_SessionNotOwned_ThrowsBankException() {
        User anotherUser = User.builder()
                .id(2L)
                .firstName("Anar")
                .lastName("Mammadov")
                .email("anar.mammadov@hkbank.az")
                .build();
        session.setUser(anotherUser);

        when(chatSessionRepository.findBySessionIdAndUserId(session.getSessionId(), 1L))
                .thenReturn(Optional.empty());

        BankException exception = assertThrows(BankException.class, () -> {
            aiSupportService.sendMessage(1L, session.getSessionId(), "Test message");
        });

        assertEquals(ErrorCode.CHAT_SESSION_NOT_FOUND, exception.getErrorCode());
        verify(chatMessageRepository, never()).save(any(ChatMessage.class));
    }

    @Test
    void getSessionHistory_Success() {
        when(chatSessionRepository.findBySessionIdAndUserId(session.getSessionId(), 1L))
                .thenReturn(Optional.of(session));
        when(chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(1L))
                .thenReturn(Arrays.asList(userMessage, assistantMessage));
        when(chatMapper.toChatMessageResponse(any(ChatMessage.class)))
                .thenReturn(messageResponse);

        List<ChatMessageResponse> messages = aiSupportService.getSessionHistory(
                1L,
                session.getSessionId()
        );

        assertNotNull(messages);
        assertEquals(2, messages.size());

        verify(chatSessionRepository).findBySessionIdAndUserId(session.getSessionId(), 1L);
        verify(chatMessageRepository).findBySessionIdOrderByCreatedAtAsc(1L);
    }

    @Test
    void getUserSessions_Success() {
        when(chatSessionRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(Arrays.asList(session));
        when(chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(1L))
                .thenReturn(Arrays.asList(userMessage, assistantMessage));
        when(chatMapper.toChatSessionResponse(any(ChatSession.class)))
                .thenReturn(sessionResponse);

        List<ChatSessionResponse> sessions = aiSupportService.getUserSessions(1L);

        assertNotNull(sessions);
        assertEquals(1, sessions.size());

        verify(chatSessionRepository).findByUserIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void closeSession_Success() {
        when(chatSessionRepository.findBySessionIdAndUserId(session.getSessionId(), 1L))
                .thenReturn(Optional.of(session));
        when(chatSessionRepository.save(any(ChatSession.class))).thenReturn(session);

        aiSupportService.closeSession(1L, session.getSessionId());

        verify(chatSessionRepository).findBySessionIdAndUserId(session.getSessionId(), 1L);
        verify(chatSessionRepository).save(argThat(s -> !s.isActive()));
    }

    @Test
    void closeSession_SessionNotFound_ThrowsBankException() {
        when(chatSessionRepository.findBySessionIdAndUserId(anyString(), eq(1L)))
                .thenReturn(Optional.empty());

        BankException exception = assertThrows(BankException.class, () -> {
            aiSupportService.closeSession(1L, "invalid-session-id");
        });

        assertEquals(ErrorCode.CHAT_SESSION_NOT_FOUND, exception.getErrorCode());
        verify(chatSessionRepository).findBySessionIdAndUserId("invalid-session-id", 1L);
        verify(chatSessionRepository, never()).save(any(ChatSession.class));
    }
}
