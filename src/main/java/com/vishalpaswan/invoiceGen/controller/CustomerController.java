package com.vishalpaswan.invoiceGen.controller;

import com.vishalpaswan.invoiceGen.service.customerService.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public/invoice-gen/api")
@RequiredArgsConstructor
@CrossOrigin("*")
public class CustomerController {
    private final CustomerService customerService;

    @GetMapping("/{invoiceId}/view")
    public ResponseEntity<?> customerInvoiceView(
            @PathVariable String invoiceId
    ) {
        return customerService.customerInvoiceView(invoiceId);
    }

}
