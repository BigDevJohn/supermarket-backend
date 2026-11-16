package jv.supermarket.user;

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
import jv.supermarket.auth.AuthResponseDTO;
import jv.supermarket.auth.TokenService;
import jv.supermarket.shared.ApiError;

@RestController
@RequestMapping("/supermarket/admin")
public class AdminController {

    final UserService userService;

    final TokenService tokenService;

    AdminController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @Operation(summary = "Employee registration", description = "Receives the new employee's data and returns a JWT token")
    @ApiResponses({
        @ApiResponse(responseCode = "201",
            description = "Employee registered successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = AuthResponseDTO.class))),
        @ApiResponse(responseCode = "400",
            description = "Registration failed: a user with this email already exists",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/createEmployee")
    public ResponseEntity<AuthResponseDTO> saveEmployee(@RequestBody @Valid User user) {
        User savedUser = userService.saveEmployee(user);
        String token = tokenService.generateToken(savedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponseDTO(token));
    }
}
