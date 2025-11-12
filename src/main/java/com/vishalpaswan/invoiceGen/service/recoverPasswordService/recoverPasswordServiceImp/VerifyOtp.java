package com.vishalpaswan.invoiceGen.service.recoverPasswordService.recoverPasswordServiceImp;

import com.vishalpaswan.invoiceGen.apiUtility.ResponseBuilder;
import com.vishalpaswan.invoiceGen.dto.requestDTO.OtpRequest;
import com.vishalpaswan.invoiceGen.dto.responseDTO.OtpStore;
import com.vishalpaswan.invoiceGen.dto.responseDTO.PasswordRecoverUserInfo;
import com.vishalpaswan.invoiceGen.entity.Users;
import com.vishalpaswan.invoiceGen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerifyOtp {
    private final UserRepository userRepository;
    private final OtpService otpService;

    private ResponseEntity<?> verifyUserOtp(OtpRequest otpRequest, String userId) {
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
                otpService.clearBoth(userId);
                log.warn("User not found during OTP verification. userId={}", userId);
                return ResponseBuilder.error(HttpStatus.NOT_FOUND, "User not found.");
            }

            Users user = findUser.get();

            // Check if OTP exists in cache
            OtpStore otpData = otpService.getOtp(user.getId());
            if (otpData == null) {
                log.warn("OTP expired or not found in cache for userId={}", userId);
                return ResponseBuilder.error(HttpStatus.GONE, "Session expired. Please request a new OTP.");
            }

            // Validate OTP input
            if (otpRequest.getOtp() == null || otpRequest.getOtp().isBlank()) {
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "OTP cannot be empty.");
            }

            // Match OTP
            if (!otpRequest.getOtp().equals(otpData.getOtp())) {
                log.warn("Incorrect OTP entered for userId={}", userId);
                return ResponseBuilder.error(HttpStatus.UNAUTHORIZED, "Incorrect OTP. Please try again.");
            }

            // OTP is valid → mark verified
            otpService.markVerified(userId);
            otpService.clearCatch(userId);

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

    public ResponseEntity<?> verifyOtpValue(OtpRequest otpRequest, String userId) {
        return verifyUserOtp(otpRequest, userId);
    }
}
