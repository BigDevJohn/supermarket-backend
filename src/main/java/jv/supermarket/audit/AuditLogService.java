package jv.supermarket.audit;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.StringJoiner;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jv.supermarket.audit.enums.Action;
import jv.supermarket.audit.enums.EntityType;
import jv.supermarket.product.Product;
import jv.supermarket.shared.customexception.ResourceNotFoundException;
import jv.supermarket.user.UserService;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserService userService;

    public AuditLogService(AuditLogRepository auditLogRepository, UserService userService) {
        this.auditLogRepository = auditLogRepository;
        this.userService = userService;
    }

    private void logAction(AuditLog auditLog) {
        auditLog.setUser(userService.getLoggedUser());
        auditLogRepository.save(auditLog);
    }

    public AuditLogDTO getAuditLogById(Long id) {
        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Audit log not found with id: " + id));
        return AuditLogDTO.from(auditLog);
    }

    public Page<AuditLogDTO> getAuditLogsByEntity(EntityType entityType, Long entityId, Pageable pageable) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId, pageable)
                .map(AuditLogDTO::from);
    }

    public Page<AuditLogDTO> getAuditLogsByEntityType(EntityType entityType, Pageable pageable) {
        return auditLogRepository.findByEntityTypeOrderByCreatedAtDesc(entityType, pageable)
                .map(AuditLogDTO::from);
    }

    public void logProductCreation(Product product) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(Action.CREATION);
        auditLog.setEntityType(EntityType.PRODUCT);
        auditLog.setEntityId(product.getId());
        auditLog.setDetails("Product created: " + product.getName() + " (" + product.getBrand() + ")");
        logAction(auditLog);
    }

    public void logProductUpdate(ProductSnapshot before, Product after) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(Action.UPDATE);
        auditLog.setEntityType(EntityType.PRODUCT);
        auditLog.setEntityId(after.getId());
        auditLog.setDetails(buildProductChanges(before, after));
        logAction(auditLog);
    }

    public void logProductDeletion(Product product) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(Action.DELETION);
        auditLog.setEntityType(EntityType.PRODUCT);
        auditLog.setEntityId(product.getId());
        auditLog.setDetails("Product deleted: " + product.getName() + " (" + product.getBrand() + ")");
        logAction(auditLog);
    }

    private String buildProductChanges(ProductSnapshot before, Product after) {
        StringJoiner changes = new StringJoiner("; ");
        addChange(changes, "name", before.name(), after.getName());
        addChange(changes, "brand", before.brand(), after.getBrand());
        addChange(changes, "price", before.price(), after.getPrice());
        addChange(changes, "description", before.description(), after.getDescription());
        addChange(changes, "available", before.available(), after.isAvailable());
        return changes.length() == 0 ? "Product updated with no field changes" : changes.toString();
    }

    private void addChange(StringJoiner changes, String field, Object before, Object after) {
        if (!Objects.equals(before, after)) {
            changes.add(field + ": " + before + " -> " + after);
        }
    }

    public record ProductSnapshot(String name, String brand, BigDecimal price, String description, Boolean available) {
        public static ProductSnapshot from(Product product) {
            return new ProductSnapshot(product.getName(), product.getBrand(), product.getPrice(),
                    product.getDescription(), product.getAvailable());
        }
    }
}
