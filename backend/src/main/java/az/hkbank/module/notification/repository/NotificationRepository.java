package az.hkbank.module.notification.repository;

import az.hkbank.module.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Notification entity operations.
 * Provides database access methods for notification management.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Finds all notifications for a user ordered by creation date descending.
     *
     * @param userId the user ID
     * @return list of notifications
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Finds all unread notifications for a user.
     *
     * @param userId the user ID
     * @return list of unread notifications
     */
    List<Notification> findByUserIdAndIsReadFalse(Long userId);

    /**
     * Counts unread notifications for a user.
     *
     * @param userId the user ID
     * @return count of unread notifications
     */
    long countByUserIdAndIsReadFalse(Long userId);

    /**
     * Marks all notifications as read for a user.
     *
     * @param userId the user ID
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    void markAllAsReadByUserId(@Param("userId") Long userId);
}
