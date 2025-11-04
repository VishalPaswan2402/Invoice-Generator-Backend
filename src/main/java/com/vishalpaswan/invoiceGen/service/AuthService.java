package com.vishalpaswan.invoiceGen.service;

import com.vishalpaswan.invoiceGen.dto.LoginRequest;
import com.vishalpaswan.invoiceGen.dto.LoginResponse;
import com.vishalpaswan.invoiceGen.dto.SignupRequest;
import com.vishalpaswan.invoiceGen.entity.Users;
import com.vishalpaswan.invoiceGen.inputValidationCheck.ValidateInput;
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

    // login
    public ResponseEntity<?> loginUser(LoginRequest loginRequest) {
        try {
            // Input validation
            if (loginRequest.getUsername() == null || loginRequest.getUsername().isBlank()) {
                return new ResponseEntity<>("Username cannot be blank.", HttpStatus.BAD_REQUEST);
            }
            if (loginRequest.getPassword() == null || loginRequest.getPassword().isBlank()) {
                return new ResponseEntity<>("Password cannot be blank.", HttpStatus.BAD_REQUEST);
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
            return new ResponseEntity<>(loginResponse, HttpStatus.OK);
        } catch (BadCredentialsException e) {
            log.warn("Invalid login attempt for username: {}", loginRequest.getUsername());
            return new ResponseEntity<>("Invalid username or password.", HttpStatus.UNAUTHORIZED);
        } catch (DisabledException e) {
            log.warn("Login attempt for disabled user: {}", loginRequest.getUsername());
            return new ResponseEntity<>("User account is disabled.", HttpStatus.FORBIDDEN);
        } catch (LockedException e) {
            log.warn("Login attempt for locked user: {}", loginRequest.getUsername());
            return new ResponseEntity<>("User account is locked.", HttpStatus.LOCKED);
        } catch (AuthenticationException e) {
            log.error("Authentication failed for user {}: {}", loginRequest.getUsername(), e.getMessage());
            return new ResponseEntity<>("Authentication failed. Please try again.", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("Unexpected error during login for user {}: {}", loginRequest.getUsername(), e.getMessage(), e);
            return new ResponseEntity<>("An unexpected error occurred. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // signup
    public ResponseEntity<?> signupUser(SignupRequest signupRequest) {
        try {
            // Validate Email
            if (!ValidateInput.isValidEmail(signupRequest.getEmail())) {
                return new ResponseEntity<>("Please enter a valid email.", HttpStatus.BAD_REQUEST);
            }

            // Check password match
            if (!signupRequest.getPassword().equals(signupRequest.getConfirmPassword())) {
                return new ResponseEntity<>("Passwords do not match.", HttpStatus.BAD_REQUEST);
            }

            // Check existing username
            if (userRepository.existsByUsername(signupRequest.getUsername())) {
                return new ResponseEntity<>("Username already exists.", HttpStatus.BAD_REQUEST);
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

            return new ResponseEntity<>(signupResponse, HttpStatus.CREATED);

        } catch (BadCredentialsException e) {
            return new ResponseEntity<>("Invalid credentials during signup.", HttpStatus.UNAUTHORIZED);
        } catch (AuthenticationException e) {
            return new ResponseEntity<>("Authentication failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
