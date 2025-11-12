package com.vishalpaswan.invoiceGen.controller;

import com.vishalpaswan.invoiceGen.security.Authorize;
import com.vishalpaswan.invoiceGen.service.mailService.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/invoice-gen/api")
@CrossOrigin("*")
public class MailController {
    private final MailService mailService;
    private final Authorize authorize;

    @GetMapping("/{ownerId}/{invoiceId}/invoiceMail")
    public ResponseEntity<?> sendInvoiceMail(
            @PathVariable String ownerId,
            @PathVariable String invoiceId,
            @RequestHeader("Authorization") String authHeader
    ) {
        ResponseEntity<?> authResult = authorize.isAuthorizes(authHeader, ownerId);
        return authResult != null ? authResult : mailService.sendInvoiceLinkOnMail(ownerId, invoiceId);
    }

}
