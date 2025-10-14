package com.vishalpaswan.invoiceGen.controller;

import com.vishalpaswan.invoiceGen.entity.Invoice;
import com.vishalpaswan.invoiceGen.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/invoice-gen/api")
@CrossOrigin("*")
public class InvoiceController {
    private final InvoiceService invoiceService;

    // save new invoice
    @PostMapping("/new-invoice")
    public ResponseEntity<?> saveNewInvoice(@Valid @RequestBody Invoice invoice){
        System.out.println("Received invoice : "+invoice);
        return invoiceService.saveNewInvoice(invoice);
    }

    // get invoice by id
    @GetMapping("/view-invoice/{id}")
    public ResponseEntity<?> getInvoiceById(@PathVariable String id){
        return invoiceService.getInvoiceById(id);
    }

    // get latest 20 invoice
    @GetMapping("/latest-20-invoice")
    public  ResponseEntity<?> getLatestInvoice(){
        return invoiceService.getLatestInvoice();
    }

    // get all invoice
    @GetMapping("/all-invoices")
    public ResponseEntity<?> getAllInvoice(){
        return invoiceService.getAllInvoice();
    }

    // delete invoice by id
    @DeleteMapping("/delete-invoice/{id}")
    public ResponseEntity<?> deleteInvoiceById(@PathVariable String id){
        return invoiceService.deleteInvoiceById(id);
    }

    // update invoice by id
    @PutMapping("/update-invoice/{id}")
    public ResponseEntity<?> updateInvoice(@Valid @RequestBody Invoice newInvoice,@PathVariable String id){
        return invoiceService.updateInvoice(newInvoice,id);
    }

}
