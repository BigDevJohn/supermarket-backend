package jv.supermarket.user;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jv.supermarket.shared.ApiError;
import jv.supermarket.shared.Response;

@RestController
@RequestMapping("/supermarket/user")
public class UserController {

    final UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Finds a user by id")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
            description = "User returned successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = User.class))),
        @ApiResponse(responseCode = "404",
            description = "User not found",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<User> getById(
            @Parameter(description = "User id") @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getById(id));
    }

    @Operation(summary = "Updates a user", description = "Updates a user based on new attributes and the current id")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
            description = "User updated successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = User.class))),
        @ApiResponse(responseCode = "404",
            description = "User not found",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{userId}/update")
    public ResponseEntity<User> updateUser(
            @Parameter(description = "New user information") @RequestBody @Valid User user,
            @Parameter(description = "User id") @PathVariable Long userId) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.updateUser(user, userId));
    }

    @Operation(summary = "Deletes a user")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
            description = "User deleted successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Response.class))),
        @ApiResponse(responseCode = "404",
            description = "No user found with the given id",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{userId}/delete")
    public ResponseEntity<Response> deleteUser(
            @Parameter(description = "User id") @PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new Response(Instant.now(), "User deleted successfully."));
    }
}
