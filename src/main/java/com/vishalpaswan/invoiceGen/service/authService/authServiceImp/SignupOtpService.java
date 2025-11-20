package com.vishalpaswan.invoiceGen.service.authService.authServiceImp;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.vishalpaswan.invoiceGen.dto.requestDTO.SignupRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignupOtpService {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int OTP_EXPIRY_MINUTES = 5;

    // cache for data storage
    private final Cache<String, SignupRequest> signupDataCache = Caffeine.newBuilder()
            .expireAfterWrite(OTP_EXPIRY_MINUTES, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    // cache for otp storage
    private final Cache<String, String> signupOtpCache = Caffeine.newBuilder()
            .expireAfterWrite(OTP_EXPIRY_MINUTES, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    // Generate 6-digit OTP
    public String generateSignupOtp() {
        return String.valueOf(secureRandom.nextInt(900000) + 100000);
    }

    // Store data
    public void storeSignupData(String signupId, SignupRequest signupRequest) {
        signupDataCache.put(signupId, signupRequest);
    }

    // Get stored data
    public SignupRequest getSignupData(String signupId) {
        return signupDataCache.getIfPresent(signupId);
    }

    // store signup otp
    public void storeSignupOtp(String signupId, String signupOtp) {
        signupOtpCache.put(signupId, signupOtp);
    }

    // get signup otp
    public String getSignupOtp(String signupId) {
        return signupOtpCache.getIfPresent(signupId);
    }

    // Clear both after use
    public void clearBoth(String signupId) {
        signupDataCache.invalidate(signupId);
        signupOtpCache.invalidate(signupId);
    }

    // Clear signupData after use
    public void clearSignupData(String signupId) {
        signupDataCache.invalidate(signupId);
    }

    // Clear signupOtp after use
    public void clearSignupOtp(String signupId) {
        signupOtpCache.invalidate(signupId);
    }

}
