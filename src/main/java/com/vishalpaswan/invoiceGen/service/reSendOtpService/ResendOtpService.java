package com.vishalpaswan.invoiceGen.service.reSendOtpService;

import com.vishalpaswan.invoiceGen.service.reSendOtpService.reSendOtpServiceImp.ResendOtpForPasswordRecovery;
import com.vishalpaswan.invoiceGen.service.reSendOtpService.reSendOtpServiceImp.ResendOtpForSignupUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResendOtpService {
    private final ResendOtpForPasswordRecovery resendOtpForPasswordRecovery;
    private final ResendOtpForSignupUser resendOtpForSignupUser;

    // resend otp for failed password recovery
    public ResponseEntity<?> resendPasswordOtp(String recoverFailedId) {
        return resendOtpForPasswordRecovery.resendRecoveryOtp(recoverFailedId);
    }

    // resend otp for failed signup verification
    public ResponseEntity<?> resendNewSignupOtp(String failedSignupId) {
        return resendOtpForSignupUser.signupOtpResend(failedSignupId);
    }

}
