package com.vishalpaswan.invoiceGen.service.recoverPasswordService.recoverPasswordServiceImp;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.vishalpaswan.invoiceGen.dto.responseDTO.OtpStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int OTP_EXPIRY_MINUTES = 5;

    // cache for OTP storage
    private final Cache<String, OtpStore> otpCache = Caffeine.newBuilder()
            .expireAfterWrite(OTP_EXPIRY_MINUTES, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    // Cache for verification status
    private final Cache<String, Boolean> isVerify = Caffeine.newBuilder()
            .expireAfterWrite(OTP_EXPIRY_MINUTES, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    // Generate 6-digit OTP
    public String generateOtp() {
        return String.valueOf(secureRandom.nextInt(900000) + 100000);
    }

    // Store OTP
    public void storeOtp(String userId, OtpStore otpStore) {
        otpCache.put(userId, otpStore);
    }

    // Get stored OTP
    public OtpStore getOtp(String userId) {
        return otpCache.getIfPresent(userId);
    }

    // Mark verified
    public void markVerified(String userId) {
        isVerify.put(userId, true);
    }

    // Check verification
    public boolean isVerified(String userId) {
        Boolean verified = isVerify.getIfPresent(userId);
        return verified != null && verified;
    }

    // Clear both after use
    public void clearBoth(String userId) {
        otpCache.invalidate(userId);
        isVerify.invalidate(userId);
    }

    // clear otpCache
    public void clearCatch(String userId) {
        otpCache.invalidate(userId);
    }

    // clear isVerify
    public void clearIsVerify(String userId) {
        isVerify.invalidate(userId);
    }

}
