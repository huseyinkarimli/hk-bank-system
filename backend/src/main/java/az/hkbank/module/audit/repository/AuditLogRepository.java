package az.hkbank.module.audit.repository;

import az.hkbank.module.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
