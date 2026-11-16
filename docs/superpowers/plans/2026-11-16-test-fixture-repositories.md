# Test Fixture Repositories Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Populate the test store state through repositories so startup does not require an authenticated user or create audit logs.

**Architecture:** Keep user creation in `UserService` because it assigns roles and creates carts. Replace audited category, product, stock, and cart-item service calls in `TestDatabaseInitializer` with direct repository persistence while preserving creation order and fixture values.

**Tech Stack:** Java 21, Spring Boot, Spring Data JPA, H2, JUnit 5, Spring Boot Test.

---

## Files and responsibilities

- Modify `src/main/java/jv/supermarket/config/TestDatabaseInitializer.java`: inject fixture repositories and persist categories, products, stocks, and the initial cart item directly.
- Modify `src/test/java/jv/supermarket/SupermarketApplicationTests.java`: add a focused regression assertion that startup fixtures do not create audit rows.
- Do not modify `AuditLogService` or production services; runtime business operations must retain their current auditing behavior.

### Task 1: Add a failing bootstrap-audit regression test

**Files:**
- Modify: `src/test/java/jv/supermarket/SupermarketApplicationTests.java`

- [ ] **Step 1: Add the repository dependency and test**

Inject `AuditLogRepository` into `SupermarketApplicationTests` and add:

```java
@Autowired
AuditLogRepository auditLogRepository;

@Test
public void testInitialTestDataDoesNotCreateAuditLogs() {
    org.junit.jupiter.api.Assertions.assertEquals(0, auditLogRepository.count());
}
```

Add the `jv.supermarket.audit.AuditLogRepository` import. The test should fail before the seeder is changed because the current product, category, and stock service calls attempt to resolve a logged user during startup.

- [ ] **Step 2: Run the focused test**

Run:

```powershell
.\mvnw.cmd -Dtest=SupermarketApplicationTests#testInitialTestDataDoesNotCreateAuditLogs test
```

Expected: FAIL during application startup with `Logged user not found`, or fail the assertion if startup proceeds far enough to persist audit rows.

### Task 2: Replace audited service calls in the test initializer

**Files:**
- Modify: `src/main/java/jv/supermarket/config/TestDatabaseInitializer.java`

- [ ] **Step 1: Replace service dependencies with repositories**

Remove `CategoryService`, `CartItemService`, `CartService`, and `ProductService` dependencies from the initializer. Add these repository dependencies:

```java
private final CartItemRepository cartItemRepository;
private final CategoryRepository categoryRepository;
private final ProductRepository productRepository;
private final StockRepository stockRepository;
```

Keep `OrderService` only if the existing constructor contract requires it; otherwise remove it as unused. Keep `UserService` for user and cart creation.

- [ ] **Step 2: Persist categories directly**

Replace each `categoryService.saveCategory(...)` call with `categoryRepository.save(...)`, preserving the existing four category names and variable order:

```java
Category c1 = categoryRepository.save(new Category("Eletrônicos"));
Category c2 = categoryRepository.save(new Category("Mobília"));
Category c3 = categoryRepository.save(new Category("Smartphones"));
Category c4 = categoryRepository.save(new Category("Cozinha"));
```

- [ ] **Step 3: Add a fixture product helper**

Add a private helper in `TestDatabaseInitializer` that creates and persists one product and its stock without invoking production services:

```java
private Product saveProduct(String name, String brand, BigDecimal price, int stockQuantity,
        String description, Category... categories) {
    Product product = new Product();
    product.setName(name);
    product.setBrand(brand);
    product.setPrice(price);
    product.setDescription(description);
    for (Category category : categories) {
        product.addCategory(category);
    }

    Product savedProduct = productRepository.save(product);
    Stock stock = stockRepository.save(new Stock(stockQuantity, savedProduct));
    savedProduct.setStock(stock);
    return productRepository.save(savedProduct);
}
```

Add the required `Product`, `ProductRepository`, `Stock`, and `StockRepository` imports. The product must be saved before the `Stock` because `Stock` uses `@MapsId`.

- [ ] **Step 4: Recreate the four product fixtures**

Replace the `ProductDTO`/`ProductRequestDTO` service calls with:

```java
Product p1 = saveProduct("Smartphone", "Samsung", new BigDecimal(3000), 20,
        "O melhor da Samsung", c3, c1);

saveProduct("Smartphone", "Xiaomi", new BigDecimal(3200), 32,
        "O mundo todo no seu bolso", c3, c1);

saveProduct("Geladeira", "Samsung", new BigDecimal(4000), 10,
        "Gela que é uma beleza!", c4, c1);

Product p4 = saveProduct("Cama de Casal", "Plumatex", new BigDecimal(1500), 5,
        "O que há de conforto para você", c2);
```

The helper must preserve the original product creation order so product IDs 1 and 4 remain the fixtures used by the tests.

- [ ] **Step 5: Persist the initial cart item directly**

Keep `cartService.getCartById(client.getId())` only if `CartService` is retained; otherwise inject `CartRepository` and retrieve the client cart directly. Create one `CartItem` for `p1`, set its cart, product, and quantity to `3`, then save it with `cartItemRepository`.

This is equivalent to the original sequence of add, add, remove, and update, but avoids unnecessary service calls and leaves the same final cart state:

```java
Cart cart = cartRepository.findById(client.getId())
        .orElseThrow(() -> new IllegalStateException("Client cart was not created"));

CartItem cartItem = new CartItem();
cartItem.setCart(cart);
cartItem.setProduct(p1);
cartItem.setQuantity(3);
cartItemRepository.save(cartItem);
cart.addItem(cartItem);
cartRepository.save(cart);
```

Use an explicit exception rather than a silent fallback if the user seeder failed to create the cart.

- [ ] **Step 6: Remove obsolete imports and fields**

Remove `ProductDTO`, `ProductRequestDTO`, `CategoryService`, `CartItemService`, `CartService`, `ProductService`, and any unused fields or suppression annotations from the initializer. Keep the initializer profile and order unchanged.

### Task 3: Verify behavior and audit isolation

**Files:**
- No new files.

- [ ] **Step 1: Run the regression test**

Run:

```powershell
.\mvnw.cmd -Dtest=SupermarketApplicationTests#testInitialTestDataDoesNotCreateAuditLogs test
```

Expected: PASS, including successful Spring context initialization.

- [ ] **Step 2: Run the complete integration test class**

Run:

```powershell
.\mvnw.cmd -Dtest=SupermarketApplicationTests test
```

Expected: PASS. This verifies the preserved product IDs, category associations, stock quantities, cart behavior, authentication annotations, and runtime audit paths.

- [ ] **Step 3: Review the final diff**

Run:

```powershell
git --no-pager diff --check
git --no-pager status --short
```

Expected: no whitespace errors; only the initializer and focused test are modified after the plan/spec commits.

- [ ] **Step 4: Commit the implementation**

```powershell
git add -- src/main/java/jv/supermarket/config/TestDatabaseInitializer.java src/test/java/jv/supermarket/SupermarketApplicationTests.java
git commit -m "fix: seed test fixtures without audit context" -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```
