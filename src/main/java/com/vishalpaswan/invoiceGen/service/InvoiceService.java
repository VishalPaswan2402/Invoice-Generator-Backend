package com.vishalpaswan.invoiceGen.service;

import com.vishalpaswan.invoiceGen.entity.Invoice;
import com.vishalpaswan.invoiceGen.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    // save invoice
    public Invoice saveInvoice(Invoice invoice){
        return invoiceRepository.save(invoice);
    }
}
