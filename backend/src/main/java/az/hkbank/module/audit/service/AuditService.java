package az.hkbank.module.audit.service;

import az.hkbank.module.audit.entity.AuditLog;
import az.hkbank.module.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing audit logs.
 * Records user actions and system events for security and compliance tracking.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Logs a user action to the audit log.
     *
     * @param userId the user ID
     * @param action the action performed
     * @param description additional description
     * @param ipAddress the IP address of the request
     */
    @Transactional
    public void log(Long userId, String action, String description, String ipAddress) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .action(action)
                    .description(description)
                    .ipAddress(ipAddress)
                    .build();

            auditLogRepository.save(auditLog);
            log.info("Audit log created: userId={}, action={}", userId, action);
        } catch (Exception e) {
            log.error("Failed to create audit log: userId={}, action={}", userId, action, e);
        }
    }
}
