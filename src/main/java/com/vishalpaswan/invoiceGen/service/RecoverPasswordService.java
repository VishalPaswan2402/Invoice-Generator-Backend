package com.vishalpaswan.invoiceGen.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.vishalpaswan.invoiceGen.apiUtility.ResponseBuilder;
import com.vishalpaswan.invoiceGen.dto.requestDTO.OtpRequest;
import com.vishalpaswan.invoiceGen.dto.requestDTO.RecoverPasswordRequest;
import com.vishalpaswan.invoiceGen.dto.requestDTO.UpdatePasswordRequest;
import com.vishalpaswan.invoiceGen.dto.responseDTO.OtpStore;
import com.vishalpaswan.invoiceGen.dto.responseDTO.PasswordRecoverUserInfo;
import com.vishalpaswan.invoiceGen.entity.Users;
import com.vishalpaswan.invoiceGen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecoverPasswordService {
    private final UserRepository userRepository;
    @Autowired
    private JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int OTP_EXPIRY_MINUTES = 1;

    // store OTP
    private final Cache<String, OtpStore> otpCache = Caffeine.newBuilder()
            .expireAfterWrite(OTP_EXPIRY_MINUTES, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    // stored verified OTP
    private final Cache<String, Boolean> isVerify = Caffeine.newBuilder()
            .expireAfterWrite(OTP_EXPIRY_MINUTES, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    // generate OTP
    public String generateOtp() {
        return String.valueOf(secureRandom.nextInt(900000) + 100000);
    }

    // find user and send otp
    public ResponseEntity<?> findUser(RecoverPasswordRequest recoverPasswordRequest) {
        try {
            log.info("Password recovery request received for username: {}", recoverPasswordRequest.getUsername());

            if (recoverPasswordRequest.getUsername().isBlank() || recoverPasswordRequest.getEmail().isBlank()) {
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Username or email is missing.");
            }

            Optional<Users> findUser = userRepository.findByUsernameAndEmail(
                    recoverPasswordRequest.getUsername(),
                    recoverPasswordRequest.getEmail()
            );

            if (findUser.isEmpty()) {
                log.warn("User not found for username: {} and email: {}",
                        recoverPasswordRequest.getUsername(), recoverPasswordRequest.getEmail());
                return ResponseBuilder.error(HttpStatus.NOT_FOUND, "User not found.");
            }

            Users user = findUser.get();

            // Generate secure OTP
            String generatedOtp = generateOtp();

            // Store OTP in cache
            otpCache.put(user.getId(), new OtpStore(user.getEmail(), generatedOtp));

            // Send email
            SimpleMailMessage sms = new SimpleMailMessage();
            sms.setTo(user.getEmail());
            sms.setSubject("OTP to recover password");
            sms.setText("Your OTP is: " + generatedOtp + "\n\nThis OTP is valid for 1 minute.");
            javaMailSender.send(sms);

            log.info("OTP successfully sent to email: {}", user.getEmail());

            PasswordRecoverUserInfo userInfo = new PasswordRecoverUserInfo(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail()
            );

            return ResponseBuilder.success(HttpStatus.OK, "OTP sent successfully.", userInfo);

        } catch (MailException ex) {
            log.error("Failed to send OTP email: {}", ex.getMessage(), ex);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "Error sending OTP email. Please try again later.");
        } catch (DataAccessException ex) {
            log.error("Database error while finding user: {}", ex.getMessage(), ex);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "Database error occurred. Please try again.");
        } catch (Exception e) {
            log.error("Unexpected error while recovering password: {}", e.getMessage(), e);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred. Please try again later.");
        }
    }

    // verify otp
    public ResponseEntity<?> verifyUserOtp(OtpRequest otpRequest, String userId) {
        try {
            log.info("Verifying OTP for userId: {}", userId);

            // Validate request integrity
            if (!otpRequest.getId().equals(userId)) {
                log.warn("Invalid OTP verification request: mismatched userId.");
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Invalid request.");
            }

            // Find user
            Optional<Users> findUser = userRepository.findById(userId);
            if (findUser.isEmpty()) {
                otpCache.invalidate(userId);
                log.warn("User not found during OTP verification. userId={}", userId);
                return ResponseBuilder.error(HttpStatus.NOT_FOUND, "User not found.");
            }

            Users user = findUser.get();

            // Check if OTP exists in cache
            OtpStore otpStore = otpCache.getIfPresent(user.getId());
            if (otpStore == null) {
                log.warn("OTP expired or not found in cache for userId={}", userId);
                return ResponseBuilder.error(HttpStatus.GONE, "Session expired. Please request a new OTP.");
            }

            // Validate OTP input
            if (otpRequest.getOtp() == null || otpRequest.getOtp().isBlank()) {
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "OTP cannot be empty.");
            }

            // Match OTP
            if (!otpRequest.getOtp().equals(otpStore.getOtp())) {
                log.warn("Incorrect OTP entered for userId={}", userId);
                return ResponseBuilder.error(HttpStatus.UNAUTHORIZED, "Incorrect OTP. Please try again.");
            }

            // OTP is valid → mark verified
            isVerify.put(userId, true);
            otpCache.invalidate(userId);

            log.info("OTP successfully verified for userId={}", userId);

            PasswordRecoverUserInfo response = new PasswordRecoverUserInfo(
                    userId,
                    user.getUsername(),
                    user.getEmail()
            );

            return ResponseBuilder.success(HttpStatus.OK, "OTP successfully verified.", response);

        } catch (DataAccessException ex) {
            log.error("Database error while finding user: {}", ex.getMessage(), ex);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "Database error occurred. Please try again.");
        } catch (Exception e) {
            log.error("Error verifying OTP for userId={}. Message={}", userId, e.getMessage(), e);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.");
        }
    }

    // update password
    public ResponseEntity<?> updatePassword(UpdatePasswordRequest updatePasswordRequest, String userId) {
        try {
            log.info("Password update request received for userId={}", userId);

            // Verify User
            if (!updatePasswordRequest.getId().equals(userId)) {
                log.warn("Invalid update password request: mismatched userId.");
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Invalid request.");
            }

            // Find user
            Optional<Users> findUser = userRepository.findById(userId);
            if (findUser.isEmpty()) {
                isVerify.invalidate(userId);
                log.warn("User not found for password update. userId={}", userId);
                return ResponseBuilder.error(HttpStatus.NOT_FOUND, "User not found.");
            }

            // Check verified OTP
            Boolean isVerified = isVerify.getIfPresent(userId);
            if (isVerified == null) {
                log.warn("Password update session expired. userId={}", userId);
                return ResponseBuilder.error(HttpStatus.GONE, "Session expired. Please verify again.");
            }

            if (updatePasswordRequest.getPassword().isBlank()) {
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Password cannot be blank.");
            }

            if (!updatePasswordRequest.getPassword().equals(updatePasswordRequest.getConfirmPassword())) {
                return ResponseBuilder.error(HttpStatus.UNPROCESSABLE_ENTITY, "Passwords do not match.");
            }

            Users user = findUser.get();
            user.setPassword(passwordEncoder.encode(updatePasswordRequest.getPassword()));
            userRepository.save(user);
            isVerify.invalidate(userId);

            log.info("Password successfully updated for userId={}", userId);
            return ResponseBuilder.success(HttpStatus.OK, "Password updated successfully.", null);

        } catch (DataAccessException ex) {
            log.error("Database error while finding user: {}", ex.getMessage(), ex);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "Database error occurred. Please try again.");
        } catch (Exception e) {
            log.error("Error updating password for userId={}: {}", userId, e.getMessage(), e);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred while updating password.");
        }
    }
}
