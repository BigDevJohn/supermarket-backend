# Audit Integration Tests Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add integration tests that prove stock audit values, customer order-cancellation auditing, audit endpoint authorization, and normal transaction/persistence behavior.

**Architecture:** Add one dedicated Spring Boot integration-test class. Use the existing test profile, H2 database, fixture initializer, `MockMvc`, Spring Security test annotations, and `AuditLogRepository`; do not change production code or fixture setup.

**Tech Stack:** Java 21, Spring Boot Test, MockMvc, Spring Security Test, Spring Data JPA, JUnit 5, H2.

---

### Task 1: Add focused audit integration test class

**Files:**
- Create: `src/test/java/jv/supermarket/AuditLogIntegrationTests.java`
- Modify: `src/test/java/jv/supermarket/SupermarketApplicationTests.java`

- [ ] **Step 1: Pin the test profile and create shared setup**

Add `@ActiveProfiles("test")` to the existing `SupermarketApplicationTests` class so its expectations use the H2 fixture instead of the global `dev` profile. Create a dedicated `@SpringBootTest(classes = SupermarketApplication.class)`, `@AutoConfigureMockMvc`, `@ActiveProfiles("test")`, `@Transactional` test class in package `jv.supermarket`. Inject `MockMvc`, `AuditLogRepository`, `OrderRepository`, and `UserRepository`. Use the existing seeded product IDs and users; do not insert additional fixtures in either test class.

```java
package jv.supermarket;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;

import jv.supermarket.audit.AuditLog;
import jv.supermarket.audit.AuditLogRepository;
import jv.supermarket.audit.enums.Action;
import jv.supermarket.audit.enums.EntityType;
import jv.supermarket.order.OrderRepository;
import jv.supermarket.user.User;
import jv.supermarket.user.UserRepository;

@SpringBootTest(classes = SupermarketApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuditLogIntegrationTests {

    @Autowired
    MockMvc mvc;

    @Autowired
    AuditLogRepository auditLogRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    UserRepository userRepository;
}
```

Keep imports grouped according to the existing test style and remove any import that remains unused after adding the methods.

- [ ] **Step 2: Add the stock-exit audit test**

Add a test authenticated as `admin@supermarket.local` that exits 5 units from seeded stock ID 2, expects HTTP 200 and quantity 27, then queries `findByEntityTypeAndEntityIdOrderByCreatedAtDesc` and asserts the newest record has `Action.UPDATE`, `EntityType.STOCK`, entity ID 2, and details containing `from 32 to 27`.

```java
@Test
@WithUserDetails("admin@supermarket.local")
void stockExitAuditsPreviousAndNewQuantities() throws Exception {
    mvc.perform(put("/supermarket/stock/2/exits")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {"quantity": 5}
                    """)
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.quantity").value(27));

    AuditLog auditLog = auditLogRepository
            .findByEntityTypeAndEntityIdOrderByCreatedAtDesc(EntityType.STOCK, 2L, Pageable.unpaged())
            .getContent()
            .get(0);

    assertEquals(Action.UPDATE, auditLog.getAction());
    assertEquals(EntityType.STOCK, auditLog.getEntityType());
    assertEquals(2L, auditLog.getEntityId());
    assertThat(auditLog.getDetails()).contains("from 32 to 27");
}
```

Include the concrete imports for `put`, `MediaType`, `Pageable`, `AuditLog`, `assertEquals`, and `assertThat`.

- [ ] **Step 3: Add the customer cancellation audit test**

Add a test authenticated as the seeded customer. Create an order through `POST /supermarket/order/create`, then resolve the newly created order without assuming an ID by looking up the seeded customer and requesting the highest-ID order:

```java
@Test
@WithUserDetails("customer@supermarket.local")
void customerCancellationCreatesAuditLog() throws Exception {
    mvc.perform(post("/supermarket/order/create").with(csrf()))
        .andExpect(status().isCreated());

    User customer = userRepository.findByEmail("customer@supermarket.local");
    Long orderId = orderRepository.findByUserId(
            customer.getId(),
            PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "id")))
        .getContent()
        .get(0)
        .getId();

    mvc.perform(delete("/supermarket/order/" + orderId + "/cancel").with(csrf()))
        .andExpect(status().isOk());

    AuditLog auditLog = auditLogRepository
        .findByEntityTypeAndEntityIdOrderByCreatedAtDesc(EntityType.ORDER, orderId, Pageable.unpaged())
        .getContent()
        .get(0);

    assertEquals(Action.UPDATE, auditLog.getAction());
    assertThat(auditLog.getDetails()).contains("Order cancelled with ID: " + orderId);
}
```

The test must use the existing cart fixture. The order query returns the creation audit and cancellation audit for the same order; selecting the newest row proves the cancellation row was written.

- [ ] **Step 4: Add audit endpoint authorization tests**

Add three tests:

1. `@WithUserDetails("admin@supermarket.local")`: `GET /supermarket/audit/by-entity-type/STOCK` returns 200.
2. `@WithUserDetails("employee@supermarket.local")`: the same request returns 200.
3. `@WithMockUser(username = "customer@supermarket.local", roles = "CUSTOMER")`: the same request returns 403.

Use `Pageable` defaults from the controller and assert only authorization/status behavior. Do not rely on audit rows created by another test because each test is transactional and isolated.

- [ ] **Step 5: Add normal transaction/persistence assertions**

For the stock-exit and customer-cancellation tests, assert both sides of each operation:

- stock quantity/order status is changed in the response or repository;
- the newest matching audit record represents the operation (order creation and cancellation intentionally produce two rows for the same order);
- the recorded entity ID and action match the business operation.

Do not add a forced repository failure test in this class. The approved design explicitly limits the integration suite to real transaction execution without mocking production repositories; a failure-injection test would be a separate service-level test.

### Task 2: Run focused and full verification

**Files:**
- Test: `src/test/java/jv/supermarket/AuditLogIntegrationTests.java`

- [ ] **Step 1: Run the new class**

Run:

```powershell
.\mvnw.cmd -Dtest=AuditLogIntegrationTests test
```

Expected: all tests in `AuditLogIntegrationTests` pass, including the stock detail assertion and the three audit endpoint authorization cases.

- [ ] **Step 2: Run the existing suite**

Run:

```powershell
.\mvnw.cmd test
```

Expected: the existing 15 tests plus the new integration tests pass with zero failures and zero errors.

- [ ] **Step 3: Inspect the final diff**

Run:

```powershell
git --no-pager diff --check
git status --short
```

Expected: no whitespace errors; only the new test file is changed by this implementation. Preserve unrelated existing worktree changes.

