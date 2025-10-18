package com.vishalpaswan.invoiceGen.controller;

import com.vishalpaswan.invoiceGen.dto.InvoiceRequest;
import com.vishalpaswan.invoiceGen.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/invoice-gen/api")
@CrossOrigin("*")
public class InvoiceController {
    private final InvoiceService invoiceService;

    // save new invoice
    @PostMapping("/{companyOwnerId}/new-invoice")
    public ResponseEntity<?> saveNewInvoice(@Valid @RequestBody InvoiceRequest invoiceRequest, @PathVariable String companyOwnerId) {
        System.out.println("Received invoice : " + invoiceRequest);
        System.out.println("CompanyId : " + companyOwnerId);
        return invoiceService.saveNewInvoice(invoiceRequest, companyOwnerId);
    }

    // get invoice by id
    @GetMapping("/{ownerId}/view-invoice/{invoiceId}")
    public ResponseEntity<?> getInvoiceById(@PathVariable String invoiceId, @PathVariable String ownerId) {
        return invoiceService.getInvoiceById(invoiceId, ownerId);
    }

    // get latest 20 invoice
    @GetMapping("/{ownerId}/latest-20-invoice")
    public ResponseEntity<?> getLatestInvoice(@PathVariable String ownerId) {
        return invoiceService.getLatestInvoice(ownerId);
    }

    // get all invoice
    @GetMapping("{ownerId}/all-invoices")
    public ResponseEntity<?> getAllInvoice(@PathVariable String ownerId) {
        return invoiceService.getAllInvoice(ownerId);
    }

    // delete invoice by id
    @DeleteMapping("{ownerId}/delete-invoice/{invoiceId}")
    public ResponseEntity<?> deleteInvoiceById(@PathVariable String invoiceId, @PathVariable String ownerId) {
        return invoiceService.deleteInvoiceById(invoiceId, ownerId);
    }

    // update invoice by id
    @PutMapping("{ownerId}/update-invoice/{invoiceId}")
    public ResponseEntity<?> updateInvoice(@Valid @RequestBody InvoiceRequest newInvoice, @PathVariable String ownerId, @PathVariable String invoiceId) {
        return invoiceService.updateInvoice(newInvoice, ownerId, invoiceId);
    }

}
