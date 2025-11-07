package com.vishalpaswan.invoiceGen.controller;

import com.vishalpaswan.invoiceGen.dto.requestDTO.InvoiceRequest;
import com.vishalpaswan.invoiceGen.security.AuthUtils;
import com.vishalpaswan.invoiceGen.security.Authorize;
import com.vishalpaswan.invoiceGen.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/invoice-gen/api")
@CrossOrigin("*")
//@CrossOrigin(origins = "http://localhost:5173")
public class InvoiceController {
    private final InvoiceService invoiceService;
    private final AuthUtils authUtils;
    private final Authorize authorize;

    // save new invoice
    @PostMapping("/{companyOwnerId}/new-invoice")
    public ResponseEntity<?> saveNewInvoice(@Valid @RequestBody InvoiceRequest invoiceRequest, @PathVariable String companyOwnerId, @RequestHeader("Authorization") String authHeader) {
        ResponseEntity<?> authResult = authorize.isAuthorizes(authHeader, companyOwnerId);
        return authResult != null ? authResult : invoiceService.saveNewInvoice(invoiceRequest, companyOwnerId);
    }

    // get invoice by id
    @GetMapping("/{ownerId}/{invoiceId}/view-invoice")
    public ResponseEntity<?> getInvoiceById(@PathVariable String invoiceId, @PathVariable String ownerId, @RequestHeader("Authorization") String authHeader) {
        ResponseEntity<?> authResult = authorize.isAuthorizes(authHeader, ownerId);
        return authResult != null ? authResult : invoiceService.getInvoiceById(invoiceId, ownerId);
    }

    // get latest 20 invoice
    @GetMapping("/{ownerId}/latest-20-invoice")
    public ResponseEntity<?> getLatestInvoice(@PathVariable String ownerId, @RequestHeader("Authorization") String authHeader) {
        ResponseEntity<?> authResult = authorize.isAuthorizes(authHeader, ownerId);
        return authResult != null ? authResult : invoiceService.getLatestInvoice(ownerId);
    }

    // get all invoice
    @GetMapping("{ownerId}/{companyId}/all-invoices")
    public ResponseEntity<?> getAllInvoice(@PathVariable String ownerId, @PathVariable String companyId, @RequestHeader("Authorization") String authHeader) {
        ResponseEntity<?> authResult = authorize.isAuthorizes(authHeader, ownerId);
        return authResult != null ? authResult : invoiceService.getAllInvoice(ownerId, companyId);
    }

    // delete invoice by id
    @DeleteMapping("{ownerId}/{invoiceId}/delete-invoice")
    public ResponseEntity<?> deleteInvoiceById(@PathVariable String invoiceId, @PathVariable String ownerId, @RequestHeader("Authorization") String authHeader) {
        ResponseEntity<?> authResult = authorize.isAuthorizes(authHeader, ownerId);
        return authResult != null ? authResult : invoiceService.deleteInvoiceById(invoiceId, ownerId);
    }

    // update invoice by id
    @PutMapping("{ownerId}/{invoiceId}/update-invoice")
    public ResponseEntity<?> updateInvoice(@Valid @RequestBody InvoiceRequest newInvoice, @PathVariable String ownerId, @PathVariable String invoiceId, @RequestHeader("Authorization") String authHeader) {
        ResponseEntity<?> authResult = authorize.isAuthorizes(authHeader, ownerId);
        return authResult != null ? authResult : invoiceService.updateInvoice(newInvoice, ownerId, invoiceId);
    }

}
