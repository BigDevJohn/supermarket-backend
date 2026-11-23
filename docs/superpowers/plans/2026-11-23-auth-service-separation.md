# Auth Service Separation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Extract login and client registration orchestration from `AuthController` into `AuthService`, and replace the registration entity parameter with `AuthRegisterRequestDTO`.

**Architecture:** `AuthController` becomes an HTTP adapter that depends only on `AuthService`. `AuthService` coordinates `UserService`, `PasswordEncoder`, and `TokenService`, while `UserService` retains persistence and customer-domain rules.

**Tech Stack:** Java 21, Spring Boot, Spring Security, JUnit 5, Mockito, Maven.

---

### Task 1: Add failing AuthService unit tests

**Files:**
- Create: `src/test/java/jv/supermarket/auth/AuthServiceTests.java`

- [ ] **Step 1: Create the unit test class**

```java
package jv.supermarket.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import jv.supermarket.shared.customexception.BadAuthRequestException;
import jv.supermarket.user.User;
import jv.supermarket.user.UserService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTests {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userService, passwordEncoder, tokenService);
    }

    @Test
    void loginWithMatchingPasswordReturnsToken() {
        AuthLoginRequestDTO dto =
                new AuthLoginRequestDTO("cliente@supermarket.local", "secret");
        User user = new User("Cliente", "cliente@supermarket.local", "encoded-password");

        when(userService.getByEmail(dto.email())).thenReturn(user);
        when(passwordEncoder.matches(dto.password(), user.getPassword())).thenReturn(true);
        when(tokenService.generateToken(user)).thenReturn("jwt-token");

        assertEquals(new AuthResponseDTO("jwt-token"), authService.login(dto));
    }

    @Test
    void loginWithWrongPasswordThrowsBadAuthRequestException() {
        AuthLoginRequestDTO dto =
                new AuthLoginRequestDTO("cliente@supermarket.local", "wrong");
        User user = new User("Cliente", "cliente@supermarket.local", "encoded-password");

        when(userService.getByEmail(dto.email())).thenReturn(user);
        when(passwordEncoder.matches(dto.password(), user.getPassword())).thenReturn(false);

        assertThrows(BadAuthRequestException.class, () -> authService.login(dto));
    }

    @Test
    void registerClientCreatesUserSavesAndReturnsToken() {
        AuthRegisterRequestDTO dto =
                new AuthRegisterRequestDTO("Cliente", "cliente@supermarket.local", "secret");

        when(userService.saveClient(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenService.generateToken(any(User.class))).thenReturn("jwt-token");

        assertEquals(new AuthResponseDTO("jwt-token"), authService.registerClient(dto));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).saveClient(userCaptor.capture());
        User savedRequest = userCaptor.getValue();
        assertEquals("Cliente", savedRequest.getName());
        assertEquals("cliente@supermarket.local", savedRequest.getEmail());
        assertEquals("secret", savedRequest.getPassword());
    }
}
```

- [ ] **Step 2: Run the test to verify the RED state**

Run:

```powershell
.\mvnw.cmd -Dtest=AuthServiceTests test
```

Expected: FAIL to compile because `AuthService` and `AuthRegisterRequestDTO` do not exist yet.

---

### Task 2: Create the auth DTO and AuthService

**Files:**
- Create: `src/main/java/jv/supermarket/auth/AuthRegisterRequestDTO.java`
- Create: `src/main/java/jv/supermarket/auth/AuthService.java`

- [ ] **Step 1: Add the registration request DTO**

```java
package jv.supermarket.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthRegisterRequestDTO(
        @NotBlank String name,
        @Email @NotBlank String email,
        @NotBlank String password) {
}
```

- [ ] **Step 2: Implement AuthService**

```java
package jv.supermarket.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jv.supermarket.shared.customexception.BadAuthRequestException;
import jv.supermarket.user.User;
import jv.supermarket.user.UserService;

@Service
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(
            UserService userService,
            PasswordEncoder passwordEncoder,
            TokenService tokenService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public AuthResponseDTO login(AuthLoginRequestDTO dto) {
        User user = userService.getByEmail(dto.email());

        if (passwordEncoder.matches(dto.password(), user.getPassword())) {
            return new AuthResponseDTO(tokenService.generateToken(user));
        }

        throw new BadAuthRequestException("Login failed: incorrect email or password");
    }

    public AuthResponseDTO registerClient(AuthRegisterRequestDTO dto) {
        User user = new User(dto.name(), dto.email(), dto.password());
        User savedUser = userService.saveClient(user);
        return new AuthResponseDTO(tokenService.generateToken(savedUser));
    }
}
```

- [ ] **Step 3: Run the unit test to verify GREEN**

Run:

```powershell
.\mvnw.cmd -Dtest=AuthServiceTests test
```

Expected: PASS, 3 tests.

---

### Task 3: Delegate AuthController operations to AuthService

**Files:**
- Modify: `src/main/java/jv/supermarket/auth/AuthController.java`

- [ ] **Step 1: Update imports and fields**

Remove these imports:

```java
import org.springframework.security.crypto.password.PasswordEncoder;
import jv.supermarket.user.User;
import jv.supermarket.user.UserService;
```

The controller fields and constructor should become:

```java
    final AuthService authService;

    AuthController(AuthService authService) {
        this.authService = authService;
    }
```

- [ ] **Step 2: Delegate login and registration**

Replace login:

```java
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid AuthLoginRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.login(dto));
    }
```

Replace the registration parameter and body:

```java
    public ResponseEntity<AuthResponseDTO> registerClient(@RequestBody @Valid AuthRegisterRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerClient(dto));
    }
```

Remove the direct `UserService`, `PasswordEncoder`, `User`, `BadAuthRequestException`, and token-generation logic from the controller.

- [ ] **Step 3: Run unit and application tests**

Run:

```powershell
.\mvnw.cmd test
```

Expected: PASS, with no compilation errors and existing application behavior unchanged.

- [ ] **Step 4: Review the resulting diff**

Run:

```powershell
git --no-pager diff --stat
git --no-pager diff -- src/main/java/jv/supermarket/auth src/test/java/jv/supermarket/auth
```

Confirm:
- `AuthController` has no `UserService`, `PasswordEncoder`, or `TokenService` dependencies.
- Registration uses `AuthRegisterRequestDTO`.
- Login and registration behavior match the existing endpoints.
