package com.vishalpaswan.invoiceGen.service;

import com.vishalpaswan.invoiceGen.dto.LoginRequest;
import com.vishalpaswan.invoiceGen.dto.LoginResponse;
import com.vishalpaswan.invoiceGen.dto.SignupRequest;
import com.vishalpaswan.invoiceGen.entity.Users;
import com.vishalpaswan.invoiceGen.inputValidationCheck.ValidateInput;
import com.vishalpaswan.invoiceGen.repository.UserRepository;
import com.vishalpaswan.invoiceGen.security.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> loginUser(LoginRequest loginRequest) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );
        Users user = (Users) authentication.getPrincipal();
        String token = authUtils.generateAccessToken(user);
        LoginResponse loginResponse = LoginResponse.builder()
                .jwt(token)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
        return new ResponseEntity<>(loginResponse, HttpStatus.OK);
    }

    // signup
    public ResponseEntity<?> signupUser(SignupRequest signupRequest) {
        if (!ValidateInput.isValidEmail(signupRequest.getEmail())) {
            return new ResponseEntity<>("Please enter a valid email.", HttpStatus.BAD_REQUEST);
        }
        if (!signupRequest.getPassword().equals(signupRequest.getConfirmPassword())) {
            return new ResponseEntity<>("Password not match.", HttpStatus.BAD_REQUEST);
        }
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            return new ResponseEntity<>("Username already exist.", HttpStatus.BAD_REQUEST);
        }
        Users savedUser = userRepository.save(Users.builder()
                .username(signupRequest.getUsername())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .email(signupRequest.getEmail())
                .build()
        );
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(savedUser.getUsername(), signupRequest.getPassword())
        );
        Users user = (Users) authentication.getPrincipal();
        String token = authUtils.generateAccessToken(user);
        LoginResponse signupResponse = LoginResponse.builder()
                .jwt(token)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
        return new ResponseEntity<>(signupResponse, HttpStatus.CREATED);
    }
}
