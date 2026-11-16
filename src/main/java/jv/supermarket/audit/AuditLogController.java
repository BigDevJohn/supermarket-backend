package jv.supermarket.audit;

import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jv.supermarket.audit.enums.EntityType;

@RestController
@RequestMapping("/supermarket/audit")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditLogDTO> getAuditLogById(@PathVariable Long id) {
        return ResponseEntity.ok(auditLogService.getAuditLogById(id));
    }

    @GetMapping("/by-entity/{entityType}/{entityId}")
    public ResponseEntity<Page<AuditLogDTO>> getAuditLogsByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @PageableDefault(size = 20) Pageable pageable) {
        EntityType type = EntityType.valueOf(entityType.toUpperCase(Locale.ROOT));
        return ResponseEntity.ok(auditLogService.getAuditLogsByEntity(type, entityId, pageable));
    }

    @GetMapping("/by-entity-type/{entityType}")
    public ResponseEntity<Page<AuditLogDTO>> getAuditLogsByEntityType(
            @PathVariable String entityType,
            @PageableDefault(size = 20) Pageable pageable) {
        EntityType type = EntityType.valueOf(entityType.toUpperCase(Locale.ROOT));
        return ResponseEntity.ok(auditLogService.getAuditLogsByEntityType(type, pageable));
    }
}
