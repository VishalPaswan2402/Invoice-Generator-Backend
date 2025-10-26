package com.vishalpaswan.invoiceGen.service;

import com.vishalpaswan.invoiceGen.dto.InvoiceResponse;
import com.vishalpaswan.invoiceGen.entity.Invoice;
import com.vishalpaswan.invoiceGen.mappersUtills.InvoiceResponseMapper;
import com.vishalpaswan.invoiceGen.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final InvoiceRepository invoiceRepository;
    private final InvoiceResponseMapper invoiceResponseMapper;

    public ResponseEntity<?> customerInvoiceView(String invoiceId) {
        Optional<Invoice> invoice = invoiceRepository.findById(invoiceId);
        if (invoice.isEmpty()) {
            return new ResponseEntity<>("Invoice not found.", HttpStatus.NO_CONTENT);
        }
        Invoice invoiceDetails = invoice.get();
        InvoiceResponse invoiceResponse = invoiceResponseMapper.mapToResponse(invoiceDetails);
        invoiceResponse.setCompanyDetails(new InvoiceResponse.CompanyDetails());
        invoiceResponse.getCompanyDetails().setName(invoiceDetails.getCompany().getCompanyName());
        invoiceResponse.getCompanyDetails().setContact(invoiceDetails.getCompany().getContact());
        invoiceResponse.getCompanyDetails().setAddress(invoiceDetails.getCompany().getAddress());
        invoiceResponse.getCompanyDetails().setEmail(invoiceDetails.getCompany().getEmail());
        return new ResponseEntity<>(invoiceResponse, HttpStatus.OK);
    }

}
