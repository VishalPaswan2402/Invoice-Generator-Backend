package com.vishalpaswan.invoiceGen.service;

import com.vishalpaswan.invoiceGen.apiUtility.ResponseBuilder;
import com.vishalpaswan.invoiceGen.dto.requestDTO.LoginRequest;
import com.vishalpaswan.invoiceGen.dto.requestDTO.SignupRequest;
import com.vishalpaswan.invoiceGen.dto.responseDTO.LoginResponse;
import com.vishalpaswan.invoiceGen.entity.Users;
import com.vishalpaswan.invoiceGen.repository.UserRepository;
import com.vishalpaswan.invoiceGen.security.AuthUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final AuthUtils authUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // login user
    public ResponseEntity<?> loginUser(LoginRequest loginRequest) {
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

    // signup new account
    public ResponseEntity<?> signupUser(SignupRequest signupRequest) {
        try {
            // Check password match
            if (!signupRequest.getPassword().equals(signupRequest.getConfirmPassword())) {
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Passwords do not match.");
            }

            // Check existing username
            if (userRepository.existsByUsername(signupRequest.getUsername())) {
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Username already exists.");
            }

            // Save user with encoded password
            Users savedUser = userRepository.save(
                    Users.builder()
                            .username(signupRequest.getUsername())
                            .password(passwordEncoder.encode(signupRequest.getPassword()))
                            .email(signupRequest.getEmail())
                            .build()
            );

            // Authenticate newly created user
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(savedUser.getUsername(), signupRequest.getPassword())
            );

            Users user = (Users) authentication.getPrincipal();
            String token = authUtils.generateAccessToken(user);

            // Build response
            LoginResponse signupResponse = LoginResponse.builder()
                    .jwt(token)
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .build();

            return ResponseBuilder.success(HttpStatus.CREATED, "Account created successfully.", signupResponse);

        } catch (BadCredentialsException e) {
            log.warn("Invalid credentials provided during signup: {}", e.getMessage());
            return ResponseBuilder.error(HttpStatus.UNAUTHORIZED, "Invalid credentials during signup.");

        } catch (AuthenticationException e) {
            log.error("Authentication failed: {}", e.getMessage(), e);
            return ResponseBuilder.error(HttpStatus.UNAUTHORIZED, "Authentication failed. Please try again.");

        } catch (Exception e) {
            log.error("Unexpected error during signup: {}", e.getMessage(), e);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.");
        }
    }
}
