package com.vishalpaswan.invoiceGen.controller;

import com.vishalpaswan.invoiceGen.entity.Invoice;
import com.vishalpaswan.invoiceGen.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/invoice-gen/api")
@CrossOrigin("*")
public class InvoiceController {
    private final InvoiceService invoiceService;
    // save invoice
    @PostMapping("/new-invoice")
    public ResponseEntity<?> saveInvoice(@RequestBody Invoice invoice){
        Invoice savedInvoice = invoiceService.saveInvoice(invoice);
        return new ResponseEntity<>(savedInvoice, HttpStatus.CREATED);
    }

    // get invoice
    @GetMapping("/view-invoice/{id}")
    public ResponseEntity<?> getInvoice(@PathVariable String id){
        Optional<Invoice> fetchInvoice=invoiceService.getInvoice(id);
        System.out.println(fetchInvoice);
        return new ResponseEntity<>(fetchInvoice,HttpStatus.OK);
    }
}
