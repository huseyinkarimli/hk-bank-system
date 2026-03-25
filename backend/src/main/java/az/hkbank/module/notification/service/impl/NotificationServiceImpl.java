package az.hkbank.module.notification.service.impl;

import az.hkbank.common.exception.BankException;
import az.hkbank.common.exception.ErrorCode;
import az.hkbank.module.notification.dto.NotificationResponse;
import az.hkbank.module.notification.entity.Notification;
import az.hkbank.module.notification.entity.NotificationType;
import az.hkbank.module.notification.mapper.NotificationMapper;
import az.hkbank.module.notification.repository.NotificationRepository;
import az.hkbank.module.notification.service.NotificationService;
import az.hkbank.module.user.entity.User;
import az.hkbank.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of NotificationService interface.
 * Handles notification creation, retrieval, and management operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createNotification(Long userId, NotificationType type, String title, String message) {
        log.info("Creating notification for user: {}, type: {}", userId, type);

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BankException(ErrorCode.USER_NOT_FOUND));

            Notification notification = Notification.builder()
                    .user(user)
                    .type(type)
                    .title(title)
                    .message(message)
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);
            log.info("Notification created successfully for user: {}", userId);

        } catch (Exception e) {
            log.error("Failed to create notification for user: {}, type: {}", userId, type, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(Long userId) {
        log.info("Fetching all notifications for user: {}", userId);

        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return notifications.stream()
                .map(notificationMapper::toNotificationResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        log.info("Fetching unread notifications for user: {}", userId);

        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalse(userId);

        return notifications.stream()
                .map(notificationMapper::toNotificationResponse)
                .toList();
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        log.info("Marking notification as read: {} for user: {}", notificationId, userId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BankException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getUser().getId().equals(userId)) {
            throw new BankException(ErrorCode.FORBIDDEN, "Unauthorized access to notification");
        }

        notification.setRead(true);
        notificationRepository.save(notification);

        log.info("Notification marked as read: {}", notificationId);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        log.info("Marking all notifications as read for user: {}", userId);

        notificationRepository.markAllAsReadByUserId(userId);

        log.info("All notifications marked as read for user: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        log.info("Fetching unread count for user: {}", userId);

        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }
}
