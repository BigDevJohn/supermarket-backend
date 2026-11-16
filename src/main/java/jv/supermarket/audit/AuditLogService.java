package jv.supermarket.audit;

import java.math.BigDecimal;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jv.supermarket.audit.enums.Action;
import jv.supermarket.audit.enums.EntityType;
import jv.supermarket.category.Category;
import jv.supermarket.order.Order;
import jv.supermarket.order.enums.OrderStatus;
import jv.supermarket.product.Product;
import jv.supermarket.shared.customexception.ResourceNotFoundException;
import jv.supermarket.stock.Stock;
import jv.supermarket.user.UserService;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public AuditLogService(AuditLogRepository auditLogRepository, UserService userService, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.userService = userService;
        this.objectMapper = objectMapper;
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
        ObjectNode details = messageDetails("Product created");
        ObjectNode attributes = details.putObject("attributes");
        attributes.put("name", product.getName());
        attributes.put("brand", product.getBrand());
        attributes.set("price", objectMapper.valueToTree(product.getPrice()));
        attributes.put("description", product.getDescription());
        attributes.put("available", product.isAvailable());
        auditLog.setDetails(details);
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
        ObjectNode details = messageDetails("Product deleted");
        ObjectNode attributes = details.putObject("attributes");
        attributes.put("name", product.getName());
        attributes.put("brand", product.getBrand());
        auditLog.setDetails(details);
        logAction(auditLog);
    }

    public void logCategoryCreation(Category savedCategory) { 
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(Action.CREATION);
        auditLog.setEntityType(EntityType.CATEGORY);
        auditLog.setEntityId(savedCategory.getId());
        ObjectNode details = messageDetails("Category created");
        details.putObject("attributes").put("name", savedCategory.getName());
        auditLog.setDetails(details);
        logAction(auditLog);
    }

    public void logCategoryDeletion(Category category) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(Action.DELETION);
        auditLog.setEntityType(EntityType.CATEGORY);
        auditLog.setEntityId(category.getId());
        ObjectNode details = messageDetails("Category deleted");
        details.putObject("attributes").put("name", category.getName());
        auditLog.setDetails(details);
        logAction(auditLog);
    }

    public void logCategoryUpdate(CategorySnapshot before, Category after) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(Action.UPDATE);
        auditLog.setEntityType(EntityType.CATEGORY);
        auditLog.setEntityId(after.getId());
        auditLog.setDetails(buildCategoryChanges(before, after));
        logAction(auditLog);
    }

     public void logStockUpdate(StockSnapshot from, Stock stock) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(Action.UPDATE);
        auditLog.setEntityType(EntityType.STOCK);
        auditLog.setEntityId(stock.getProduct().getId());
        auditLog.setDetails(changeDetails("Stock updated", "quantity", from.quantity(), stock.getQuantity()));
        logAction(auditLog);
    }

    public void logStockCreation(Stock stock) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(Action.CREATION);
        auditLog.setEntityType(EntityType.STOCK);
        auditLog.setEntityId(stock.getProduct().getId());
        ObjectNode details = messageDetails("Stock created");
        ObjectNode attributes = details.putObject("attributes");
        attributes.put("quantity", stock.getQuantity());
        attributes.put("productId", stock.getProduct().getId());
        auditLog.setDetails(details);
        logAction(auditLog);
    }

    private ObjectNode buildProductChanges(ProductSnapshot before, Product after) {
        ObjectNode details = messageDetails("Product updated");
        ArrayNode changes = details.putArray("changes");
        addChange(changes, "name", before.name(), after.getName());
        addChange(changes, "brand", before.brand(), after.getBrand());
        addChange(changes, "price", before.price(), after.getPrice());
        addChange(changes, "description", before.description(), after.getDescription());
        addChange(changes, "available", before.available(), after.isAvailable());
        if (changes.isEmpty()) {
            details.put("message", "Product updated with no field changes");
        }
        return details;
    }

    private ObjectNode buildCategoryChanges(CategorySnapshot before, Category after) {
        ObjectNode details = messageDetails("Category updated");
        ArrayNode changes = details.putArray("changes");
        addChange(changes, "name", before.name(), after.getName());
        if (changes.isEmpty()) {
            details.put("message", "Category updated with no field changes");
        }
        return details;
    }

    private void addChange(ArrayNode changes, String field, Object before, Object after) {
        if (!Objects.equals(before, after)) {
            ObjectNode change = changes.addObject();
            change.put("field", field);
            change.set("before", objectMapper.valueToTree(before));
            change.set("after", objectMapper.valueToTree(after));
        }
    }

    public void logOrderCreation(Order order){
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(Action.CREATION);
        auditLog.setEntityType(EntityType.ORDER);
        auditLog.setEntityId(order.getId());
        ObjectNode details = messageDetails("Order created");
        details.putObject("attributes").put("orderId", order.getId());
        auditLog.setDetails(details);
        logAction(auditLog);
    }

    public void logOrderCancellation(OrderStatus before, Order order){
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(Action.UPDATE);
        auditLog.setEntityType(EntityType.ORDER);
        auditLog.setEntityId(order.getId());
        auditLog.setDetails(changeDetails("Order cancelled", "status", before, order.getStatus()));
        logAction(auditLog);
    }

    private ObjectNode messageDetails(String message) {
        ObjectNode details = objectMapper.createObjectNode();
        details.put("message", message);
        return details;
    }

    private ObjectNode changeDetails(String message, String field, Object before, Object after) {
        ObjectNode details = messageDetails(message);
        ArrayNode changes = details.putArray("changes");
        addChange(changes, field, before, after);
        return details;
    }

    public record ProductSnapshot(String name, String brand, BigDecimal price, String description, Boolean available) {
        public static ProductSnapshot from(Product product) {
            return new ProductSnapshot(product.getName(), product.getBrand(), product.getPrice(),
                    product.getDescription(), product.getAvailable());
        }
    }

    public record CategorySnapshot(String name) {
        public static CategorySnapshot from(Category category) {
            return new CategorySnapshot(category.getName());
        }
    }

    public record StockSnapshot(Integer quantity) {
        public static StockSnapshot from(Stock stock) {
            return new StockSnapshot(stock.getQuantity());
        }
    }

}
