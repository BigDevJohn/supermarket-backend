package jv.supermarket.audit;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import jv.supermarket.audit.enums.EntityType;
import jv.supermarket.user.User;


public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUser(User user);
    Page<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            EntityType entityType, Long entityId, Pageable pageable);
    Page<AuditLog> findByEntityTypeOrderByCreatedAtDesc(EntityType entityType, Pageable pageable);
}
