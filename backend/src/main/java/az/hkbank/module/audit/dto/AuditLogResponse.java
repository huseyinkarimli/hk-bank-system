package az.hkbank.module.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for audit log details.
 * Contains audit log information for display to admins.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    private Long id;
    private Long userId;
    private String action;
    private String description;
    private String ipAddress;
    private String entityType;
    private Long entityId;
    private LocalDateTime createdAt;
}
