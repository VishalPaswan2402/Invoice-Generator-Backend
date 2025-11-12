package com.vishalpaswan.invoiceGen.service.customerService;

import com.vishalpaswan.invoiceGen.mappersUtills.InvoiceResponseMapper;
import com.vishalpaswan.invoiceGen.repository.InvoiceRepository;
import com.vishalpaswan.invoiceGen.service.customerService.customerServiceImp.ViewInvoice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {
    private final InvoiceRepository invoiceRepository;
    private final InvoiceResponseMapper invoiceResponseMapper;
    private final ViewInvoice viewInvoice;

    // view invoice for customers
    public ResponseEntity<?> customerInvoiceView(String invoiceId) {
        return viewInvoice.getInvoice(invoiceId);
    }
    
}
