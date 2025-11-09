package com.vishalpaswan.invoiceGen.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class SendEmailService {
    @Autowired
    private JavaMailSender javaMailSender;

    private void sendMail(String sender, String receiver, String subject, String text) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, true);
        mimeMessageHelper.setFrom(sender);
        mimeMessageHelper.setTo(receiver);
        mimeMessageHelper.setSubject(subject);
        mimeMessageHelper.setText(text, true);
        javaMailSender.send(message);
        if (true) {
            throw new MailSendException("Failed");
        }
    }

    public void sendOtpEmail(String sender, String receiver, String otp) throws MessagingException {
        String subject = "OTP to recover your password.";
        String htmlContent = """
                    <div style="font-family: Arial, sans-serif; padding: 20px; background-color: #f6f8fa;">
                        <div style="max-width: 500px; margin: auto; background: #fff; border-radius: 8px; 
                                    box-shadow: 0 2px 6px rgba(0,0,0,0.1); padding: 25px;">
                            <h2 style="color: #333; text-align: center;">Your One-Time Password (OTP)</h2>
                            <p style="font-size: 16px; color: #555; text-align: center;">
                                Use the following OTP to complete your password recovery process.
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
        sendMail(sender, receiver, subject, htmlContent);
    }

    public void sendInvoiceLinkMail(String sender, String receiver, String invoiceUrl, String companyName) throws MessagingException {
        String subject = "Your Invoice URL is Ready.";
        String htmlContent = """
                    <div style="font-family: Arial, sans-serif; background-color: #f6f8fa; padding: 20px;">
                        <div style="max-width: 600px; margin: auto; background-color: #ffffff; border-radius: 8px;
                                    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); padding: 25px;">
                
                            <h2 style="color: #333; text-align: center; margin-bottom: 10px;">
                                🧾 Your Invoice is Ready
                            </h2>
                            <p style="color: #555; font-size: 15px; text-align: center;">
                                Thank you for your purchase! Your invoice has been successfully generated.
                            </p>
                
                            <div style="text-align: center; margin: 30px 0;">
                                <a href="%s" style="background-color: #2e7d32; color: #fff; text-decoration: none; 
                                    padding: 12px 24px; border-radius: 5px; font-weight: bold; font-size: 16px;
                                    display: inline-block;">
                                    🔗 View Invoice
                                </a>
                            </div>
                
                            <div style="font-size: 14px; color: #777; text-align: center; line-height: 1.5;">
                                <p>If the button above doesn't work, copy and paste the following link into your browser:</p>
                                <p style="word-break: break-all; color: #2e7d32;">%s</p>
                            </div>
                
                            <hr style="margin: 30px 0;">
                            <p style="font-size: 12px; color: #aaa; text-align: center;">
                                © %d %s. All rights reserved.
                            </p>
                        </div>
                    </div>
                """.formatted(invoiceUrl, invoiceUrl, java.time.Year.now().getValue(), companyName);
        sendMail(sender, receiver, subject, htmlContent);
    }

}
