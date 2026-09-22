package jv.supermarket.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jv.supermarket.shared.ApiError;
import jv.supermarket.shared.customexception.BadAuthRequestException;
import jv.supermarket.user.User;
import jv.supermarket.user.UserService;

@RestController
@RequestMapping("/supermarket/auth")
public class AuthController {

    final TokenService tokenService;

    final UserService userService;

    final PasswordEncoder passwordEncoder;

    AuthController(TokenService tokenService, PasswordEncoder passwordEncoder, UserService userService) {
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    @Operation(summary = "User login", description = "Receives email and password and returns a JWT token")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
            description = "User logged in successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = AuthResponseDTO.class))),
        @ApiResponse(responseCode = "400",
            description = "Login failed: incorrect email or password",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid AuthLoginRequestDTO dto) {
        User user = userService.getByEmail(dto.email());

        if (passwordEncoder.matches(dto.password(), user.getPassword())) {
            String token = tokenService.generateToken(user);
            return ResponseEntity.status(HttpStatus.OK).body(new AuthResponseDTO(token));
        }
        throw new BadAuthRequestException("Login failed: incorrect email or password");
    }

    @Operation(summary = "Client registration", description = "Receives the new client's data and returns a JWT token")
    @ApiResponses({
        @ApiResponse(responseCode = "201",
            description = "Client registered successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = AuthResponseDTO.class))),
        @ApiResponse(responseCode = "400",
            description = "Registration failed: a user with this email already exists",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> registerClient(@RequestBody @Valid User user) {
        User savedUser = userService.saveClient(user);
        String token = tokenService.generateToken(savedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponseDTO(token));
    }
}
