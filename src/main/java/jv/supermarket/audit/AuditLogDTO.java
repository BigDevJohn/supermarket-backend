package jv.supermarket.audit;

import java.time.LocalDateTime;

import jv.supermarket.audit.enums.Action;
import jv.supermarket.audit.enums.EntityType;

public record AuditLogDTO(
        Long id,
        Action action,
        EntityType entityType,
        Long entityId,
        String details,
        LocalDateTime createdAt,
        Long userId,
        String userName) {

    public static AuditLogDTO from(AuditLog auditLog) {
        return new AuditLogDTO(
                auditLog.getId(),
                auditLog.getAction(),
                auditLog.getEntityType(),
                auditLog.getEntityId(),
                auditLog.getDetails(),
                auditLog.getCreatedAt(),
                auditLog.getUser().getId(),
                auditLog.getUser().getName());
    }
}
