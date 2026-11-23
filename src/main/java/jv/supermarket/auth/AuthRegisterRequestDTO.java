package jv.supermarket.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthRegisterRequestDTO(
        @NotBlank String name,
        @Email @NotBlank String email,
        @NotBlank String password) {
}
