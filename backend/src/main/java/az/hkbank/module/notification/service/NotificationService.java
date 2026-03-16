package az.hkbank.module.notification.service;

import az.hkbank.module.notification.dto.NotificationResponse;
import az.hkbank.module.notification.entity.NotificationType;

import java.util.List;

/**
 * Service interface for notification operations.
 * Defines methods for creating, retrieving, and managing user notifications.
 */
public interface NotificationService {

    /**
     * Creates a new notification for a user.
     *
     * @param userId the user ID
     * @param type the notification type
     * @param title the notification title
     * @param message the notification message
     */
    void createNotification(Long userId, NotificationType type, String title, String message);

    /**
     * Retrieves all notifications for a user.
     *
     * @param userId the user ID
     * @return list of notification responses
     */
    List<NotificationResponse> getUserNotifications(Long userId);

    /**
     * Retrieves unread notifications for a user.
     *
     * @param userId the user ID
     * @return list of unread notification responses
     */
    List<NotificationResponse> getUnreadNotifications(Long userId);

    /**
     * Marks a notification as read.
     *
     * @param notificationId the notification ID
     * @param userId the user ID
     */
    void markAsRead(Long notificationId, Long userId);

    /**
     * Marks all notifications as read for a user.
     *
     * @param userId the user ID
     */
    void markAllAsRead(Long userId);

    /**
     * Gets the count of unread notifications for a user.
     *
     * @param userId the user ID
     * @return count of unread notifications
     */
    long getUnreadCount(Long userId);
}
