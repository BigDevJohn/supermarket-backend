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
