package com.vishalpaswan.invoiceGen.service.reSendOtpService.reSendOtpServiceImp;

import com.vishalpaswan.invoiceGen.apiUtility.ResponseBuilder;
import com.vishalpaswan.invoiceGen.dto.responseDTO.OtpStore;
import com.vishalpaswan.invoiceGen.dto.responseDTO.VerifyingUserInfo;
import com.vishalpaswan.invoiceGen.entity.Users;
import com.vishalpaswan.invoiceGen.repository.UserRepository;
import com.vishalpaswan.invoiceGen.service.mailService.mailServiceImp.OtpMailTemplate;
import com.vishalpaswan.invoiceGen.service.recoverPasswordService.recoverPasswordServiceImp.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResendOtpForPasswordRecovery {
    @Value("${spring.mail.username}")
    private String siteEmail;
    private final OtpService otpService;
    private final OtpMailTemplate otpMailTemplate;
    private final UserRepository userRepository;

    private ResponseEntity<?> resendRecoverPasswordOtp(String recoverFailedId) {
        try {
            if (recoverFailedId.isBlank()) {
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Invalid request. Try again!");
            }

            Optional<Users> isUserExist = userRepository.findById(recoverFailedId);
            if (isUserExist.isEmpty()) {
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "User account not found.");
            }

            OtpStore oldOtpData = otpService.getOtp(recoverFailedId);
            if (oldOtpData == null) {
                return ResponseBuilder.error(HttpStatus.GONE, "Session expired.");
            }

            String userEmail = oldOtpData.getEmail();
            String newOtp = otpService.generateOtp();
            OtpStore newOtpStore = new OtpStore(userEmail, newOtp);

            otpService.clearBoth(recoverFailedId);
            otpService.storeOtp(recoverFailedId, newOtpStore);

            VerifyingUserInfo verifyingUserInfo = new VerifyingUserInfo(
                    recoverFailedId,
                    isUserExist.get().getUsername(),
                    userEmail
            );
            otpMailTemplate.sendYourOtp(siteEmail, userEmail, newOtp);
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

    public ResponseEntity<?> resendRecoveryOtp(String recoverFailedId) {
        return resendRecoverPasswordOtp(recoverFailedId);
    }

}
