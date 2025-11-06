package com.vishalpaswan.invoiceGen.controller;

import com.vishalpaswan.invoiceGen.dto.requestDTO.LoginRequest;
import com.vishalpaswan.invoiceGen.dto.requestDTO.SignupRequest;
import com.vishalpaswan.invoiceGen.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/invoice-gen/api")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AuthController {
    private final AuthService authService;

    // login user
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.loginUser(loginRequest);
    }

    // signup
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest signupRequest) {
        return authService.signupUser(signupRequest);
    }
}
