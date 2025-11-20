package com.vishalpaswan.invoiceGen.service.authService.authServiceImp;

import com.vishalpaswan.invoiceGen.apiUtility.ResponseBuilder;
import com.vishalpaswan.invoiceGen.dto.requestDTO.OtpRequest;
import com.vishalpaswan.invoiceGen.dto.requestDTO.SignupRequest;
import com.vishalpaswan.invoiceGen.dto.responseDTO.LoginResponse;
import com.vishalpaswan.invoiceGen.entity.Users;
import com.vishalpaswan.invoiceGen.repository.UserRepository;
import com.vishalpaswan.invoiceGen.security.AuthUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerifyOtpAndSaveUser {
    private final AuthenticationManager authenticationManager;
    private final AuthUtils authUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SignupOtpService signupOtpService;

    private ResponseEntity<?> verifySaveNewUser(OtpRequest otpRequest) {
        try {
            // Check password match
            if (otpRequest.getOtp().isBlank() || otpRequest.getId().isBlank()) {
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Signup Id and OTP is required.");
            }

            String storedOtp = signupOtpService.getSignupOtp(otpRequest.getId());
            if (storedOtp == null) {
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Session expired, try again.");
            }

            if (!storedOtp.equals(otpRequest.getOtp())) {
                return ResponseBuilder.error(HttpStatus.PARTIAL_CONTENT, "Please enter correct OTP.");
            }

            SignupRequest signupRequest = signupOtpService.getSignupData(otpRequest.getId());
            if (signupRequest == null) {
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Session expires, try again.");
            }

            // Save user with encoded password
            Users savedUser = userRepository.save(
                    Users.builder()
                            .username(signupRequest.getUsername())
                            .password(passwordEncoder.encode(signupRequest.getPassword()))
                            .email(signupRequest.getEmail())
                            .build()
            );

            // clear cache
            signupOtpService.clearBoth(otpRequest.getId());

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

    public ResponseEntity<?> saveNewUser(OtpRequest otpRequest) {
        return verifySaveNewUser(otpRequest);
    }

}
