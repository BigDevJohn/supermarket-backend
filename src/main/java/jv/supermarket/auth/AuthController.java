package jv.supermarket.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping("/supermarket/auth")
public class AuthController {

    final AuthService authService;

    AuthController(AuthService authService) {
        this.authService = authService;
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
        return ResponseEntity.status(HttpStatus.OK).body(authService.login(dto));
    }

    @Operation(summary = "Client registration", description = "Receives the new client's data and returns a JWT token")
    @ApiResponses({
        @ApiResponse(responseCode = "201",
            description = "Client registered successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = AuthResponseDTO.class))),
        @ApiResponse(responseCode = "400",
            description = "Registration failed: invalid data provided",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> registerClient(@RequestBody @Valid AuthRegisterRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerClient(dto));
    }
}
