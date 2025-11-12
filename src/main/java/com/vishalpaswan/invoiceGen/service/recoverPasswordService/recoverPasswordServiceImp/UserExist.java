package com.vishalpaswan.invoiceGen.service.recoverPasswordService.recoverPasswordServiceImp;

import com.vishalpaswan.invoiceGen.apiUtility.ResponseBuilder;
import com.vishalpaswan.invoiceGen.dto.requestDTO.RecoverPasswordRequest;
import com.vishalpaswan.invoiceGen.dto.responseDTO.OtpStore;
import com.vishalpaswan.invoiceGen.dto.responseDTO.PasswordRecoverUserInfo;
import com.vishalpaswan.invoiceGen.entity.Users;
import com.vishalpaswan.invoiceGen.repository.UserRepository;
import com.vishalpaswan.invoiceGen.service.mailService.mailServiceImp.OtpMailTemplate;
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
public class UserExist {
    @Value("${spring.mail.username}")
    private String siteEmail;
    private final UserRepository userRepository;
    //    private final SendEmailService sendEmailService;
    private final OtpService otpService;
    private final OtpMailTemplate sendOtpMail;

    private ResponseEntity<?> findUsers(RecoverPasswordRequest recoverPasswordRequest) {
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
            String generatedOtp = otpService.generateOtp();

            // Store OTP in cache
            otpService.storeOtp(user.getId(), new OtpStore(user.getEmail(), generatedOtp));

            // Send email
            sendOtpMail.sendYourOtp(siteEmail, user.getEmail(), generatedOtp);

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

    public ResponseEntity<?> isUserExist(RecoverPasswordRequest recoverPasswordRequest) {
        return findUsers(recoverPasswordRequest);
    }

}
