package az.hkbank.module.notification;

import az.hkbank.common.exception.BankException;
import az.hkbank.common.exception.ErrorCode;
import az.hkbank.module.notification.dto.NotificationResponse;
import az.hkbank.module.notification.entity.Notification;
import az.hkbank.module.notification.entity.NotificationType;
import az.hkbank.module.notification.mapper.NotificationMapper;
import az.hkbank.module.notification.repository.NotificationRepository;
import az.hkbank.module.notification.service.impl.NotificationServiceImpl;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationServiceImpl.
 * Tests notification creation, retrieval, and management operations.
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User user;
    private Notification notification;
    private NotificationResponse notificationResponse;

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

        notification = Notification.builder()
                .id(1L)
                .user(user)
                .type(NotificationType.TRANSACTION)
                .title("Köçürmə uğurla tamamlandı")
                .message("Köçürmə uğurla tamamlandı: 100.00 AZN")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationResponse = NotificationResponse.builder()
                .id(1L)
                .type(NotificationType.TRANSACTION)
                .title("Köçürmə uğurla tamamlandı")
                .message("Köçürmə uğurla tamamlandı: 100.00 AZN")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createNotification_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.createNotification(
                1L,
                NotificationType.TRANSACTION,
                "Köçürmə uğurla tamamlandı",
                "Köçürmə uğurla tamamlandı: 100.00 AZN"
        );

        verify(userRepository).findById(1L);
        verify(notificationRepository).save(argThat(n ->
                n.getType() == NotificationType.TRANSACTION &&
                n.getTitle().equals("Köçürmə uğurla tamamlandı") &&
                !n.isRead()
        ));
    }

    @Test
    void createNotification_UserNotFound_DoesNotThrow() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> {
            notificationService.createNotification(
                    1L,
                    NotificationType.TRANSACTION,
                    "Test",
                    "Test message"
            );
        });

        verify(userRepository).findById(1L);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void getUserNotifications_Success() {
        List<Notification> notifications = Arrays.asList(notification, notification);

        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(notifications);
        when(notificationMapper.toNotificationResponse(any(Notification.class))).thenReturn(notificationResponse);

        List<NotificationResponse> responses = notificationService.getUserNotifications(1L);

        assertNotNull(responses);
        assertEquals(2, responses.size());

        verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(1L);
        verify(notificationMapper, times(2)).toNotificationResponse(any(Notification.class));
    }

    @Test
    void getUnreadCount_Success() {
        when(notificationRepository.countByUserIdAndIsReadFalse(1L)).thenReturn(5L);

        long count = notificationService.getUnreadCount(1L);

        assertEquals(5L, count);
        verify(notificationRepository).countByUserIdAndIsReadFalse(1L);
    }

    @Test
    void markAsRead_Success() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.markAsRead(1L, 1L);

        verify(notificationRepository).findById(1L);
        verify(notificationRepository).save(argThat(n -> n.isRead()));
    }

    @Test
    void markAsRead_NotOwner_ThrowsBankException() {
        User anotherUser = User.builder()
                .id(2L)
                .firstName("Rauf")
                .lastName("Aliyev")
                .email("rauf.aliyev@hkbank.az")
                .build();
        notification.setUser(anotherUser);

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        BankException exception = assertThrows(BankException.class, () -> {
            notificationService.markAsRead(1L, 1L);
        });

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
        verify(notificationRepository).findById(1L);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void markAsRead_NotificationNotFound_ThrowsBankException() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());

        BankException exception = assertThrows(BankException.class, () -> {
            notificationService.markAsRead(1L, 1L);
        });

        assertEquals(ErrorCode.NOTIFICATION_NOT_FOUND, exception.getErrorCode());
        verify(notificationRepository).findById(1L);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void markAllAsRead_Success() {
        doNothing().when(notificationRepository).markAllAsReadByUserId(1L);

        notificationService.markAllAsRead(1L);

        verify(notificationRepository).markAllAsReadByUserId(1L);
    }

    @Test
    void getUnreadNotifications_Success() {
        List<Notification> unreadNotifications = Arrays.asList(notification);

        when(notificationRepository.findByUserIdAndIsReadFalse(1L)).thenReturn(unreadNotifications);
        when(notificationMapper.toNotificationResponse(any(Notification.class))).thenReturn(notificationResponse);

        List<NotificationResponse> responses = notificationService.getUnreadNotifications(1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertFalse(responses.get(0).isRead());

        verify(notificationRepository).findByUserIdAndIsReadFalse(1L);
        verify(notificationMapper).toNotificationResponse(any(Notification.class));
    }
}
