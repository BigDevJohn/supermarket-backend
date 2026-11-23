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
