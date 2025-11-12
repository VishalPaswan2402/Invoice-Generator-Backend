package com.vishalpaswan.invoiceGen.service.authService;

import com.vishalpaswan.invoiceGen.dto.requestDTO.LoginRequest;
import com.vishalpaswan.invoiceGen.dto.requestDTO.SignupRequest;
import com.vishalpaswan.invoiceGen.service.authService.authServiceImp.Login;
import com.vishalpaswan.invoiceGen.service.authService.authServiceImp.Signup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final Login login;
    private final Signup signup;

    // login user
    public ResponseEntity<?> loginUser(LoginRequest loginRequest) {
        return login.loginUser(loginRequest);
    }

    // signup new account
    public ResponseEntity<?> signupUser(SignupRequest signupRequest) {
        return signup.signupUser(signupRequest);
    }

}
