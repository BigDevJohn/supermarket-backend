package jv.supermarket.audit;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.JsonNode;

import jv.supermarket.audit.enums.Action;
import jv.supermarket.audit.enums.EntityType;

public record AuditLogDTO(
        Long id,
        Action action,
        EntityType entityType,
        Long entityId,
        @tools.jackson.databind.annotation.JsonSerialize(using = Jackson2JsonNodeSerializer.class) JsonNode details,
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
