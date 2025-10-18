package com.vishalpaswan.invoiceGen.inputValidationCheck;

import java.util.regex.Pattern;

public class ValidateInput {

    // Regex for Indian mobile number (10 digits, starting from 6-9)
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");

    // Simple regex for email validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    // Method to validate phone number
    public static boolean isValidPhoneNumber(String phone) {
        if (phone == null) return false;
        return PHONE_PATTERN.matcher(phone).matches();
    }

    // Method to validate email
    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }

}
