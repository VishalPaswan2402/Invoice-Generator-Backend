package com.vishalpaswan.invoiceGen.service.mailService.mailServiceImp;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class SendMail {
    @Autowired
    private JavaMailSender javaMailSender;

    private void mailSending(String sender, String receiver, String subject, String text) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, true);
        mimeMessageHelper.setFrom(sender);
        mimeMessageHelper.setTo(receiver);
        mimeMessageHelper.setSubject(subject);
        mimeMessageHelper.setText(text, true);
        javaMailSender.send(message);
    }

    public void sendYourMail(String sender, String receiver, String subject, String text) throws MessagingException {
        mailSending(sender, receiver, subject, text);
    }

}
