package com.vishalpaswan.invoiceGen.service.authService.authServiceImp;

import com.vishalpaswan.invoiceGen.apiUtility.ResponseBuilder;
import com.vishalpaswan.invoiceGen.dto.requestDTO.SignupRequest;
import com.vishalpaswan.invoiceGen.dto.responseDTO.VerifyingUserInfo;
import com.vishalpaswan.invoiceGen.repository.UserRepository;
import com.vishalpaswan.invoiceGen.service.mailService.mailServiceImp.SignupOtpTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class Signup {
    @Value("${spring.mail.username}")
    private String siteEmail;
    private final UserRepository userRepository;
    private final SignupOtpService signupOtpService;
    private final SignupOtpTemplate signupOtpTemplate;

    private ResponseEntity<?> userSignup(SignupRequest signupRequest) {
        try {
            // Check password match
            if (!signupRequest.getPassword().equals(signupRequest.getConfirmPassword())) {
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Passwords do not match.");
            }

            // Check existing username
            if (userRepository.existsByUsername(signupRequest.getUsername())) {
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Username already exists.");
            }

            String signupId = UUID.randomUUID().toString();
            String signupOtp = signupOtpService.generateSignupOtp();

            signupOtpService.storeSignupData(signupId, signupRequest);
            signupOtpService.storeSignupOtp(signupId, signupOtp);

            // send otp mail
            signupOtpTemplate.sendSignupOtp(siteEmail, signupRequest.getEmail(), signupOtp);

            VerifyingUserInfo signupOtpVerify = new VerifyingUserInfo(signupId, signupRequest.getUsername(), signupRequest.getEmail());
            return ResponseBuilder.success(HttpStatus.OK, "OTP sent successfully.", signupOtpVerify);

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

    public ResponseEntity<?> signupUser(SignupRequest signupRequest) {
        return userSignup(signupRequest);
    }

}
