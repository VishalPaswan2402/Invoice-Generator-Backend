package com.vishalpaswan.invoiceGen.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.vishalpaswan.invoiceGen.dto.*;
import com.vishalpaswan.invoiceGen.entity.Users;
import com.vishalpaswan.invoiceGen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RecoverPasswordService {
    private final UserRepository userRepository;
    @Autowired
    private JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;

    // store otp
    private final Cache<String, OtpStore> otpCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    // stored verified otp
    private final Cache<String, Boolean> isVerify = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
//            .maximumSize(1000)
            .build();

    public String generateOtp() {
        return String.valueOf(new Random().nextInt(900000) + 100000);
    }

    // find user and send otp
    public ResponseEntity<?> findUser(@RequestBody RecoverPasswordRequest recoverPasswordRequest) {
        System.out.println("Req data : " + recoverPasswordRequest);
        if (recoverPasswordRequest.getUsername().isEmpty() || recoverPasswordRequest.getEmail().isEmpty()) {
            return new ResponseEntity<>("Username or password is missing.", HttpStatus.BAD_REQUEST);
        }
        Optional<Users> findUser = userRepository.findByUsernameAndEmail(recoverPasswordRequest.getUsername(), recoverPasswordRequest.getEmail());
        if (findUser.isEmpty()) {
            return new ResponseEntity<>("User not found.", HttpStatus.NO_CONTENT);
        }
        Users user = findUser.get();
        String generatedOtp = generateOtp();
        otpCache.put(user.getId(), new OtpStore(user.getEmail(), generatedOtp));
        SimpleMailMessage sms = new SimpleMailMessage();
        sms.setTo(user.getEmail());
        sms.setSubject("OTP to recover password.");
        sms.setText("Your OTP is : " + generatedOtp);
        javaMailSender.send(sms);
        PasswordRecoverUserInfo userInfo = new PasswordRecoverUserInfo(user.getId(), user.getUsername(), user.getEmail());
        return new ResponseEntity<>(userInfo, HttpStatus.OK);
    }

    // verify otp
    public ResponseEntity<?> verifyUserOtp(OtpRequest otpRequest, String userId) {
        System.out.println("OTP Request : " + otpRequest);
        if (!otpRequest.getId().equals(userId)) {
            return new ResponseEntity<>("Invalid request.", HttpStatus.BAD_REQUEST);
        }
        Optional<Users> findUser = userRepository.findById(userId);
        if (findUser.isEmpty()) {
            otpCache.invalidate(userId);
            return new ResponseEntity<>("User not found.", HttpStatus.BAD_REQUEST);
        }
        Users user = findUser.get();
        OtpStore otpStore = otpCache.getIfPresent(user.getId());
        if (otpStore == null) {
            return new ResponseEntity<>("Session expired, try again later.", HttpStatus.BAD_REQUEST);
        }
        if (!otpRequest.getOtp().equals(otpStore.getOtp())) {
            return new ResponseEntity<>("Incorrect, try again with correct OTP.", HttpStatus.NON_AUTHORITATIVE_INFORMATION);
        }
        isVerify.put(userId, true);
        otpCache.invalidate(userId);
        PasswordRecoverUserInfo passwordRecoverUserInfo = new PasswordRecoverUserInfo(userId, user.getUsername(), user.getEmail());
        return new ResponseEntity<>(passwordRecoverUserInfo, HttpStatus.OK);
    }

    // update password
    public ResponseEntity<?> updatePassword(UpdatePasswordRequest updatePasswordRequest, String userId) {
        if (!updatePasswordRequest.getId().equals(userId)) {
            return new ResponseEntity<>("Invalid request.", HttpStatus.BAD_REQUEST);
        }
        Optional<Users> findUser = userRepository.findById(userId);
        if (findUser.isEmpty()) {
            isVerify.invalidate(userId);
            return new ResponseEntity<>("User not found.", HttpStatus.BAD_REQUEST);
        }
        Boolean isVerified = isVerify.getIfPresent(userId);
        if (isVerified == null) {
            return new ResponseEntity<>("Session expired, try again later.", HttpStatus.CONFLICT);
        }
        if (!updatePasswordRequest.getPassword().equals(updatePasswordRequest.getConfirmPassword())) {
            return new ResponseEntity<>("Password not match.", HttpStatus.NON_AUTHORITATIVE_INFORMATION);
        }
        //updating password...
        Users user = findUser.get();
        user.setPassword(passwordEncoder.encode(updatePasswordRequest.getPassword()));
        userRepository.save(user);
        isVerify.invalidate(userId);
        return new ResponseEntity<>("Password update successfully.", HttpStatus.OK);
    }

}
