package com.vishalpaswan.invoiceGen.controller;

import com.vishalpaswan.invoiceGen.dto.OtpRequest;
import com.vishalpaswan.invoiceGen.dto.RecoverPasswordRequest;
import com.vishalpaswan.invoiceGen.dto.UpdatePasswordRequest;
import com.vishalpaswan.invoiceGen.service.RecoverPasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public/invoice-gen/api")
@RequiredArgsConstructor
@CrossOrigin("*")
public class RecoverPasswordController {
    private final RecoverPasswordService recoverPasswordService;

    // find user
    @PostMapping("/recover-password")
    public ResponseEntity<?> findUser(@Valid @RequestBody RecoverPasswordRequest recoverPasswordRequest) {
        return recoverPasswordService.findUser(recoverPasswordRequest);
    }

    // verify otp
    @PostMapping("/{userId}/verify-otp")
    public ResponseEntity<?> verifyUserOtp(@RequestBody OtpRequest otpRequest, @PathVariable String userId) {
        return recoverPasswordService.verifyUserOtp(otpRequest, userId);
    }

    // update password
    @PostMapping("/{userId}/update-password")
    public ResponseEntity<?> updatePassword(@RequestBody UpdatePasswordRequest updatePasswordRequest, @PathVariable String userId) {
        return recoverPasswordService.updatePassword(updatePasswordRequest, userId);
    }

}
