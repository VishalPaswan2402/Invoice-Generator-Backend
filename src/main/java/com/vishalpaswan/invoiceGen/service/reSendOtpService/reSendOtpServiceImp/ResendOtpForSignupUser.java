package com.vishalpaswan.invoiceGen.service.reSendOtpService.reSendOtpServiceImp;

import com.vishalpaswan.invoiceGen.apiUtility.ResponseBuilder;
import com.vishalpaswan.invoiceGen.dto.requestDTO.SignupRequest;
import com.vishalpaswan.invoiceGen.dto.responseDTO.VerifyingUserInfo;
import com.vishalpaswan.invoiceGen.service.authService.authServiceImp.SignupOtpService;
import com.vishalpaswan.invoiceGen.service.mailService.mailServiceImp.SignupOtpTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResendOtpForSignupUser {
    @Value("${spring.mail.username}")
    private String siteEmail;
    private final SignupOtpService signupOtpService;
    private final SignupOtpTemplate signupOtpTemplate;

    private ResponseEntity<?> resendSignupOtp(String signupFailedId) {
        try {
            if (signupFailedId.isBlank()) {
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Invalid request. Try again!");
            }

            SignupRequest signupRequest = signupOtpService.getSignupData(signupFailedId);
            if (signupRequest == null) {
                return ResponseBuilder.error(HttpStatus.GONE, "Session expired.");
            }

            String userEmail = signupRequest.getEmail();
            String newOtp = signupOtpService.generateSignupOtp();

            signupOtpService.clearBoth(signupFailedId);
            signupOtpService.storeSignupOtp(signupFailedId, newOtp);
            signupOtpService.storeSignupData(signupFailedId, signupRequest);

            signupOtpTemplate.sendSignupOtp(siteEmail, userEmail, newOtp);
            VerifyingUserInfo verifyingUserInfo = new VerifyingUserInfo(signupFailedId, signupRequest.getUsername(), userEmail);

            return ResponseBuilder.success(HttpStatus.OK, "OTP resent successfully.", verifyingUserInfo);

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

    public ResponseEntity<?> signupOtpResend(String signupFailedId) {
        return resendSignupOtp(signupFailedId);
    }

}
