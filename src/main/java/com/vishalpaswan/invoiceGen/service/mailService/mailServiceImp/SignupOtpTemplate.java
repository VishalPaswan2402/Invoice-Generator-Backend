package com.vishalpaswan.invoiceGen.service.mailService.mailServiceImp;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignupOtpTemplate {
    private final SendMail sendMail;

    private void sendSignupMailOtp(String sender, String receiver, String otp) throws MessagingException {
        String subject = "Your OTP for Signup";
        String htmlContent = """
                    <div style="font-family: Arial, sans-serif; padding: 20px; background-color: #f6f8fa;">
                        <div style="max-width: 500px; margin: auto; background: #fff; border-radius: 8px; 
                                    box-shadow: 0 2px 6px rgba(0,0,0,0.1); padding: 25px;">
                            <h2 style="color: #333; text-align: center;">Your One-Time Password (OTP)</h2>
                            <p style="font-size: 16px; color: #555; text-align: center;">
                                Use the following OTP to complete your signup process.
                            </p>
                            <div style="text-align: center; margin: 20px 0;">
                                <span style="font-size: 32px; font-weight: bold; color: #2e7d32;">%s</span>
                            </div>
                            <p style="text-align: center; font-size: 14px; color: #777;">
                                This OTP is valid for <strong>1 minute</strong>.
                            </p>
                            <hr style="margin: 30px 0;">
                            <p style="font-size: 13px; color: #999; text-align: center;">
                                If you didn’t request this, please ignore this email.
                            </p>
                            <p style="font-size: 12px; color: #aaa; text-align: center;">
                                © %d InvoiceGen. All rights reserved.
                            </p>
                        </div>
                    </div>
                """.formatted(otp, java.time.Year.now().getValue());
        sendMail.sendYourMail(sender, receiver, subject, htmlContent);
    }

    public void sendSignupOtp(String sender, String receiver, String otp) throws MessagingException {
        sendSignupMailOtp(sender, receiver, otp);
    }
}
