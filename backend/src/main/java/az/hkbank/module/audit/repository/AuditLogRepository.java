package az.hkbank.module.audit.repository;

import az.hkbank.module.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for AuditLog entity operations.
 * Provides database access methods for audit log management.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Finds all audit logs for a specific user.
     *
     * @param userId the user ID
     * @return list of audit logs
     */
    List<AuditLog> findByUserId(Long userId);

    /**
     * Finds all audit logs for a specific user ordered by creation date descending.
     *
     * @param userId the user ID
     * @return list of audit logs
     */
    List<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Finds all audit logs by action type.
     *
     * @param action the action type
     * @return list of audit logs
     */
    List<AuditLog> findByAction(String action);

    /**
     * Finds audit logs by entity type and entity ID.
     *
     * @param entityType the entity type
     * @param entityId the entity ID
     * @return list of audit logs
     */
    List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);

    /**
     * Finds audit logs within a date range.
     *
     * @param from start date
     * @param to end date
     * @return list of audit logs
     */
    List<AuditLog> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}
