# Structured Audit Details and Indexes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Store audit details as typed JSON and add indexes for entity, type, timestamp, and user history queries.

**Architecture:** Keep `AuditLog` as the single audit entity, change its details payload from `String` to Jackson `JsonNode`, and build payloads centrally in `AuditLogService`. Add one Flyway V4 migration that converts existing text details to JSON and creates the three query indexes. Update integration tests to assert JSON fields and cancellation status transitions.

**Tech Stack:** Java 21, Spring Boot 4.1, Jackson, Hibernate JSON JDBC type, MySQL/Flyway, H2 test profile, JUnit 5, MockMvc.

---

## Audit event documentation

Every audit record contains the common metadata below:

| Field | Description |
| --- | --- |
| `action` | Operation type: `CREATION`, `UPDATE`, or `DELETION`. |
| `entityType` | Audited resource type: `PRODUCT`, `CATEGORY`, `ORDER`, or `STOCK`. |
| `entityId` | Identifier of the affected resource. For stock events, this is the related product ID. |
| `userId` / `userName` | Authenticated user who performed the operation. |
| `createdAt` | Timestamp assigned when the audit record is created. |
| `details` | Structured JSON payload described below; legacy null values remain null. |

Creation and deletion events use an `attributes` object to preserve the relevant state:

| Event | `details.message` | `details.attributes` |
| --- | --- | --- |
| Product creation | `Product created` | `name`, `brand`, `price`, `description`, `available` |
| Product deletion | `Product deleted` | `name`, `brand` |
| Category creation | `Category created` | `name` |
| Category deletion | `Category deleted` | `name` |
| Stock creation | `Stock created` | `quantity`, `productId` |
| Order creation | `Order created` | `orderId` |

Update events use a `changes` array. Each entry contains `field`, `before`, and
`after`; only fields whose values changed are included. Product updates can
record `name`, `brand`, `price`, `description`, and `available`. Category
updates record `name`, and stock updates record `quantity`.

For example, a stock update is represented as:

```json
{
  "message": "Stock updated",
  "changes": [
    { "field": "quantity", "before": 32, "after": 27 }
  ]
}
```

Order cancellation is an update event with `message` equal to `Order cancelled`
and a `status` change, including the status before cancellation and
`CANCELLED` after cancellation:

```json
{
  "message": "Order cancelled",
  "changes": [
    { "field": "status", "before": "WAITING_PAYMENT", "after": "CANCELLED" }
  ]
}
```

If a product or category update does not change any tracked field, the payload
still identifies the event with `message` set to `Product updated with no field
changes` or `Category updated with no field changes`, respectively, and an
empty `changes` array.

The V4 migration wraps pre-existing non-null text details as
`{ "message": "original text" }`; null details remain null. The structured
payload is returned unchanged by the audit DTO, so API consumers can query
numeric, boolean, string, and null values without parsing a formatted string.

---

### Task 1: Add the JSON-and-index schema migration

**Files:**
- Create: `src/main/resources/db/migration/V4__structure_audit_details_and_add_indexes.sql`

- [ ] **Step 1: Write the migration**

Create a MySQL-compatible migration that preserves old text details and then creates the required indexes:

```sql
ALTER TABLE audit_logs
    ADD COLUMN details_json JSON NULL;

UPDATE audit_logs
SET details_json = JSON_OBJECT('message', details)
WHERE details IS NOT NULL;

ALTER TABLE audit_logs
    DROP COLUMN details;

ALTER TABLE audit_logs
    CHANGE COLUMN details_json details JSON NULL;

CREATE INDEX idx_audit_logs_entity
    ON audit_logs (entity_type, entity_id, created_at);

CREATE INDEX idx_audit_logs_entity_type
    ON audit_logs (entity_type, created_at);

CREATE INDEX idx_audit_logs_user
    ON audit_logs (user_id, created_at);
```

The migration must leave null details null and wrap every existing non-null text value as `{ "message": "original text" }`. Do not modify V3; Flyway must apply V4 after existing installations already have V3.

- [ ] **Step 2: Validate migration syntax against repository conventions**

Run:

```powershell
git diff --check
```

Review the migration for MySQL syntax, semicolon termination, index names, and the absence of duplicate index names before moving to Java changes.

- [ ] **Step 3: Commit the schema migration**

Run:

```powershell
git add src/main/resources/db/migration/V4__structure_audit_details_and_add_indexes.sql
git commit -m "feat: index and structure audit details" -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

### Task 2: Map audit details to JSON

**Files:**
- Modify: `src/main/java/jv/supermarket/audit/AuditLog.java`
- Modify: `src/main/java/jv/supermarket/audit/AuditLogDTO.java`

- [ ] **Step 1: Change the entity field and constructors**

Replace `String details` with `JsonNode details`. Annotate the field with Hibernate's JSON JDBC type:

```java
@JdbcTypeCode(SqlTypes.JSON)
@Column(columnDefinition = "json")
private JsonNode details;
```

Update the getter, setter, and both constructors to use `JsonNode`. Preserve the existing nullable behavior because creation/deletion/update methods always provide details but the database column allows null for migrated legacy rows.

- [ ] **Step 2: Change the DTO contract**

Change the `AuditLogDTO.details` component from `String` to `JsonNode`. Keep the record field order unchanged so all other response properties remain compatible. `AuditLogDTO.from` should return `auditLog.getDetails()` directly.

- [ ] **Step 3: Compile the mapping**

Run:

```powershell
.\mvnw.cmd -DskipTests compile
```

Expected: compilation succeeds with no type errors from constructors, getters, setters, or DTO mapping.

### Task 3: Build structured payloads in the audit service

**Files:**
- Modify: `src/main/java/jv/supermarket/audit/AuditLogService.java`
- Modify: `src/main/java/jv/supermarket/order/OrderService.java`

- [ ] **Step 1: Add reusable JSON payload helpers**

Inject `ObjectMapper` into `AuditLogService` and add helpers that build `ObjectNode` payloads:

```java
private ObjectNode messageDetails(String message) {
    ObjectNode details = objectMapper.createObjectNode();
    details.put("message", message);
    return details;
}

private ObjectNode changeDetails(String message, String field, Object before, Object after) {
    ObjectNode details = messageDetails(message);
    ArrayNode changes = details.putArray("changes");
    ObjectNode change = changes.addObject();
    change.put("field", field);
    change.set("before", objectMapper.valueToTree(before));
    change.set("after", objectMapper.valueToTree(after));
    return details;
}
```

Use `attributes` objects for creation/deletion payloads and `changes` arrays for updates. Preserve all currently audited values:

- Product: name, brand, price, description, available.
- Category: name.
- Stock: quantity.
- Order cancellation: status.

Do not concatenate old/new values into a string. `logAction` must continue assigning the logged user and saving the entity.

- [ ] **Step 2: Capture the previous order status**

Change `logOrderCancellation` to accept the previous `OrderStatus` and the updated `Order`. In `OrderService.cancelOrder`, capture `OrderStatus before = order.getStatus()` before setting `CANCELLED`, then pass `before` to the audit service in both admin and customer branches.

The cancellation JSON must contain:

```json
{
  "message": "Order cancelled",
  "changes": [
    {"field": "status", "before": "WAITING_PAYMENT", "after": "CANCELLED"}
  ]
}
```

- [ ] **Step 3: Update all details assertions at the service boundary**

Keep entity/action/type/actor behavior unchanged. Ensure every audit creation method produces valid JSON nodes and that `AuditLogService` remains usable inside the caller's transaction.

- [ ] **Step 4: Commit the JSON mapping and service changes**

Run:

```powershell
git add src/main/java/jv/supermarket/audit/AuditLog.java src/main/java/jv/supermarket/audit/AuditLogDTO.java src/main/java/jv/supermarket/audit/AuditLogService.java src/main/java/jv/supermarket/order/OrderService.java
git commit -m "feat: store structured audit details" -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

### Task 4: Update integration coverage for the new contract

**Files:**
- Modify: `src/test/java/jv/supermarket/AuditLogIntegrationTests.java`

- [ ] **Step 1: Update stock audit assertions**

Change the stock exit and stock listing assertions from substring checks to JSON assertions:

```java
assertThat(auditLog.getDetails().path("message").asText()).isEqualTo("Stock updated");
assertThat(auditLog.getDetails().path("changes").get(0).path("field").asText()).isEqualTo("quantity");
assertThat(auditLog.getDetails().path("changes").get(0).path("before").asInt()).isEqualTo(32);
assertThat(auditLog.getDetails().path("changes").get(0).path("after").asInt()).isEqualTo(27);
```

For MockMvc responses, assert `$.content[0].details.message`, `$.content[0].details.changes[0].field`, and the numeric before/after paths.

- [ ] **Step 2: Update cancellation assertions**

Assert the newest order audit contains `details.message == "Order cancelled"`, a `status` change, `before == "WAITING_PAYMENT"`, `after == "CANCELLED"`, and the expected customer `userId`.

- [ ] **Step 3: Verify JSON persistence through repository-backed assertions**

The stock and cancellation tests must read their newest records back through `AuditLogRepository` and assert the `JsonNode` object fields before checking the HTTP listing response. This proves H2's generated schema and Hibernate JSON mapping work in the test profile without requiring MySQL; no separate fixture or mock is needed.

- [ ] **Step 4: Run focused tests**

Run:

```powershell
.\mvnw.cmd -Dtest=AuditLogIntegrationTests -Dspring.profiles.active=test test
```

Expected: all audit integration tests pass and verify typed JSON rather than formatted strings.

### Task 5: Run full verification

**Files:**
- Test: `src/test/java/jv/supermarket/AuditLogIntegrationTests.java`
- Migration: `src/main/resources/db/migration/V4__structure_audit_details_and_add_indexes.sql`

- [ ] **Step 1: Run the complete test suite**

Run:

```powershell
.\mvnw.cmd -Dspring.profiles.active=test test
```

Expected: all existing and new tests pass with zero failures and zero errors.

- [ ] **Step 2: Check the final diff**

Run:

```powershell
git diff --check
git status --short
```

Expected: no whitespace errors and only the planned migration, audit Java files, and test updates are changed.
