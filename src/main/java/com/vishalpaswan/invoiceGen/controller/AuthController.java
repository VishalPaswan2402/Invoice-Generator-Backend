package com.vishalpaswan.invoiceGen.controller;

import com.vishalpaswan.invoiceGen.dto.LoginRequest;
import com.vishalpaswan.invoiceGen.dto.LoginResponse;
import com.vishalpaswan.invoiceGen.dto.SignupRequest;
import com.vishalpaswan.invoiceGen.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    // login user
    @PostMapping("/login")
    public LoginResponse loginUser(@RequestBody LoginRequest loginRequest) {
        return authService.loginUser(loginRequest);
    }

    // signup
    @PostMapping("/signup")
    public String signup(@RequestBody SignupRequest signupRequest) {
        return authService.signupUser(signupRequest);
    }
}
