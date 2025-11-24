package com.vishalpaswan.invoiceGen.controller;

import com.vishalpaswan.invoiceGen.service.reSendOtpService.ResendOtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/public/invoice-gen/api")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ResendOtpController {
    private final ResendOtpService resendOtpService;

    // resend recover password otp
    @PostMapping("/recover-password/resend")
    public ResponseEntity<?> resendRecoverPasswordOtp(@RequestBody Map<String, String> bodyData) {
        String recoverFailedId = bodyData.get("recoverFailedId");
        return resendOtpService.resendPasswordOtp(recoverFailedId);
    }

    // resend signup email verification otp
    @PostMapping("/signup-user/resend")
    public ResponseEntity<?> resendSignupOtp(@RequestBody Map<String, String> bodyData) {
        String failedSignupId = bodyData.get("failedSignupId");
        return resendOtpService.resendNewSignupOtp(failedSignupId);
    }

}
