package jv.supermarket.audit;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import jv.supermarket.audit.enums.EntityType;
import jv.supermarket.user.User;


public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUser(User user);
    List<AuditLog> findByEntityId(Long entityId);
    List<AuditLog> findByEntityType(EntityType entityType);
}
