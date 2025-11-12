package com.vishalpaswan.invoiceGen.service.mailService.mailServiceImp;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvoiceMailTemplate {
    private final SendMail sendMail;

    private void sendInvoiceLinkMail(String sender, String receiver, String invoiceUrl, String companyName) throws MessagingException {
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
        sendMail.sendYourMail(sender, receiver, subject, htmlContent);
    }

    public void sendInvoiceLink(String sender, String receiver, String invoiceUrl, String companyName) throws MessagingException {
        sendInvoiceLinkMail(sender, receiver, invoiceUrl, companyName);
    }

}
