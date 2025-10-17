package com.vishalpaswan.invoiceGen.service;

import com.vishalpaswan.invoiceGen.dto.LoginRequest;
import com.vishalpaswan.invoiceGen.dto.LoginResponse;
import com.vishalpaswan.invoiceGen.dto.SignupRequest;
import com.vishalpaswan.invoiceGen.entity.Users;
import com.vishalpaswan.invoiceGen.repository.UserRepository;
import com.vishalpaswan.invoiceGen.security.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final AuthUtils authUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // login
    public LoginResponse loginUser(LoginRequest loginRequest) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        Users user = (Users) authentication.getPrincipal();
        String token = authUtils.generateAccessToken(user);
        return LoginResponse.builder()
                .jwt(token)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    // signup
    public String signupUser(SignupRequest signupRequest) {
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            return "Username already exist.";
        }
        Users savedUser = userRepository.save(Users.builder()
                .username(signupRequest.getUsername())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .email(signupRequest.getEmail())
                .build()
        );
        return "User registered successfully";
    }
}
