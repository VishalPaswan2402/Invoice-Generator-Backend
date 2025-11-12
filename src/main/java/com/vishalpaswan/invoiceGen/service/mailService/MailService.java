package com.vishalpaswan.invoiceGen.service.mailService;

import com.vishalpaswan.invoiceGen.service.mailService.mailServiceImp.InvoiceMail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {
    private final InvoiceMail invoiceMail;

    // send invoice link on email of client
    public ResponseEntity<?> sendInvoiceLinkOnMail(String ownerId, String invoiceId) {
        return invoiceMail.sendLink(ownerId, invoiceId);
    }

}
