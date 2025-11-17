package com.vishalpaswan.invoiceGen.service.recoverPasswordService.recoverPasswordServiceImp;

import com.vishalpaswan.invoiceGen.apiUtility.ResponseBuilder;
import com.vishalpaswan.invoiceGen.dto.requestDTO.UpdatePasswordRequest;
import com.vishalpaswan.invoiceGen.entity.Users;
import com.vishalpaswan.invoiceGen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChangePassword {
    @Value("${spring.mail.username}")
    private String siteEmail;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;

    private ResponseEntity<?> updatePassword(UpdatePasswordRequest updatePasswordRequest, String userId) {
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
                otpService.clearBoth(userId);
                log.warn("User not found for password update. userId={}", userId);
                return ResponseBuilder.error(HttpStatus.NOT_FOUND, "User not found.");
            }

            // Check verified OTP
            boolean isVerified = otpService.isVerified(userId);
            if (!isVerified) {
                log.warn("Password update session expired. userId={}", userId);
                return ResponseBuilder.error(HttpStatus.GONE, "Session expired. Please verify again.");
            }

            if (updatePasswordRequest.getPassword().isBlank()) {
                return ResponseBuilder.error(HttpStatus.PARTIAL_CONTENT, "Password cannot be blank.");
            }

            if (!updatePasswordRequest.getPassword().equals(updatePasswordRequest.getConfirmPassword())) {
                return ResponseBuilder.error(HttpStatus.PARTIAL_CONTENT, "Passwords do not match.");
            }

            Users user = findUser.get();
            user.setPassword(passwordEncoder.encode(updatePasswordRequest.getPassword()));
            userRepository.save(user);
            otpService.clearIsVerify(userId);

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

    public ResponseEntity<?> resetPassword(UpdatePasswordRequest updatePasswordRequest, String userId) {
        return updatePassword(updatePasswordRequest, userId);
    }

}
