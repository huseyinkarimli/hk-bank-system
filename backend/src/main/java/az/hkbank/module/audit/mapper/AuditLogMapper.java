package az.hkbank.module.audit.mapper;

import az.hkbank.module.audit.dto.AuditLogResponse;
import az.hkbank.module.audit.entity.AuditLog;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for AuditLog entity transformations.
 * Converts between AuditLog entities and DTOs.
 */
@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    /**
     * Converts AuditLog entity to AuditLogResponse DTO.
     *
     * @param auditLog the audit log entity
     * @return audit log response DTO
     */
    AuditLogResponse toAuditLogResponse(AuditLog auditLog);
}
