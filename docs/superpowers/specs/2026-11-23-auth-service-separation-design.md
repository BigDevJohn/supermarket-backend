# Auth Service Separation Design

## Goal

Move authentication and client-registration orchestration out of `AuthController` and into an `AuthService` in the `jv.supermarket.auth` package. Replace the registration request's `User` entity parameter with an auth-specific DTO.

## Existing Behavior

- `POST /supermarket/auth/login` accepts `AuthLoginRequestDTO`, searches for the user by email, compares the password hash, and returns a JWT.
- `POST /supermarket/auth/register` accepts a `User` entity, delegates persistence to `UserService.saveClient`, and returns a JWT.
- Incorrect passwords and invalid registration data continue to throw the existing authentication exceptions.

## Architecture

`AuthController` remains the HTTP adapter:

- Handles request mappings and HTTP response codes.
- Validates request DTOs.
- Calls `AuthService`.
- Does not depend directly on `UserService`, `PasswordEncoder`, or `TokenService`.

`AuthService` contains the authentication use cases:

- `login(AuthLoginRequestDTO dto): AuthResponseDTO`
- `registerClient(AuthRegisterRequestDTO dto): AuthResponseDTO`

Dependencies:

- `UserService` for user lookup and persistence.
- `PasswordEncoder` for password comparison.
- `TokenService` for JWT generation.

`AuthRegisterRequestDTO` is a record with `name`, `email`, and `password` fields. It applies the same Bean Validation constraints that were previously applied to `User` fields:

- Name: not blank.
- Email: valid and not blank.
- Password: not blank.

The service converts the register DTO into a `User` before calling `UserService.saveClient`.

## Request Flow

### Login

1. The controller receives and validates `AuthLoginRequestDTO`.
2. The controller calls `AuthService.login(dto)`.
3. `AuthService` gets the user through `UserService.getByEmail`.
4. `AuthService` compares the supplied password with the stored password hash.
5. A matching password produces a JWT from `TokenService`; an incorrect password throws `BadAuthRequestException`.

### Registration

1. The controller receives and validates `AuthRegisterRequestDTO`.
2. The controller calls `AuthService.registerClient(dto)`.
3. `AuthService` creates a `User` from the DTO.
4. `AuthService` calls `UserService.saveClient(user)`.
5. `AuthService` generates a JWT for the saved user.
6. The controller returns HTTP 201 with `AuthResponseDTO`.

## Error Handling

Existing exceptions and HTTP mappings remain unchanged:

- Login failure returns the existing bad-authentication response.
- Registration duplicate-email and invalid-data behavior remain in `UserService` and the existing exception handling.
- Validation errors use Bean Validation through `@Valid`.

## Testing

Add focused unit tests for `AuthService`:

- Successful login returns the generated token.
- Incorrect password throws `BadAuthRequestException`.
- Registration converts the DTO to `User`, saves the customer, and returns the generated token.

Tests must mock only external collaborators; `AuthService` behavior remains directly observable. Existing application tests must continue to pass.
