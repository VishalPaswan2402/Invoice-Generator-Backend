package com.vishalpaswan.invoiceGen.service.authService.authServiceImp;

import com.vishalpaswan.invoiceGen.apiUtility.ResponseBuilder;
import com.vishalpaswan.invoiceGen.dto.requestDTO.LoginRequest;
import com.vishalpaswan.invoiceGen.dto.responseDTO.LoginResponse;
import com.vishalpaswan.invoiceGen.entity.Users;
import com.vishalpaswan.invoiceGen.security.AuthUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class Login {
    private final AuthenticationManager authenticationManager;
    private final AuthUtils authUtils;

    private ResponseEntity<?> userLogin(LoginRequest loginRequest) {
        try {
            // Input validation
            if (loginRequest.getUsername() == null || loginRequest.getUsername().isBlank()) {
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Username cannot be blank.");
            }
            if (loginRequest.getPassword() == null || loginRequest.getPassword().isBlank()) {
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Password cannot be blank.");
            }

            // Authenticate user credentials
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // Get authenticated user
            Users user = (Users) authentication.getPrincipal();

            // Generate JWT token
            String token = authUtils.generateAccessToken(user);

            // Build response
            LoginResponse loginResponse = LoginResponse.builder()
                    .jwt(token)
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .build();

            log.info("User '{}' logged in successfully.", user.getUsername());
            return ResponseBuilder.success(HttpStatus.OK, "Login Successfully.", loginResponse);

        } catch (BadCredentialsException e) {
            log.warn("Invalid login attempt for username: {}", loginRequest.getUsername());
            return ResponseBuilder.error(HttpStatus.UNAUTHORIZED, "Invalid username or password.");

        } catch (DisabledException e) {
            log.warn("Login attempt for disabled user: {}", loginRequest.getUsername());
            return ResponseBuilder.error(HttpStatus.FORBIDDEN, "User account is disabled.");

        } catch (LockedException e) {
            log.warn("Login attempt for locked user: {}", loginRequest.getUsername());
            return ResponseBuilder.error(HttpStatus.LOCKED, "User account is locked.");

        } catch (AuthenticationException e) {
            log.error("Authentication failed for user {}: {}", loginRequest.getUsername(), e.getMessage());
            return ResponseBuilder.error(HttpStatus.UNAUTHORIZED, "Authentication failed. Please try again.");

        } catch (Exception e) {
            log.error("Unexpected error during login for user {}: {}", loginRequest.getUsername(), e.getMessage(), e);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.");
        }
    }

    public ResponseEntity<?> loginUser(LoginRequest loginRequest) {
        return userLogin(loginRequest);
    }

}
