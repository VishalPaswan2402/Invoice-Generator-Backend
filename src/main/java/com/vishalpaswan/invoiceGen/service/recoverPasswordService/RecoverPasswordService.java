package com.vishalpaswan.invoiceGen.service.recoverPasswordService;

import com.vishalpaswan.invoiceGen.dto.requestDTO.OtpRequest;
import com.vishalpaswan.invoiceGen.dto.requestDTO.RecoverPasswordRequest;
import com.vishalpaswan.invoiceGen.dto.requestDTO.UpdatePasswordRequest;
import com.vishalpaswan.invoiceGen.service.recoverPasswordService.recoverPasswordServiceImp.ChangePassword;
import com.vishalpaswan.invoiceGen.service.recoverPasswordService.recoverPasswordServiceImp.UserExist;
import com.vishalpaswan.invoiceGen.service.recoverPasswordService.recoverPasswordServiceImp.VerifyOtp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecoverPasswordService {
    private final UserExist userExist;
    private final VerifyOtp verifyOtp;
    private final ChangePassword changePassword;

    // find user and send otp
    public ResponseEntity<?> findUser(RecoverPasswordRequest recoverPasswordRequest) {
        return userExist.isUserExist(recoverPasswordRequest);
    }

    // verify otp
    public ResponseEntity<?> verifyUserOtp(OtpRequest otpRequest, String userId) {
        return verifyOtp.verifyOtpValue(otpRequest, userId);
    }
    
    // update password
    public ResponseEntity<?> updatePassword(UpdatePasswordRequest updatePasswordRequest, String userId) {
        return changePassword.resetPassword(updatePasswordRequest, userId);
    }

}
