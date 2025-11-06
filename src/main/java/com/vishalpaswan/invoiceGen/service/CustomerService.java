package com.vishalpaswan.invoiceGen.service;

import com.vishalpaswan.invoiceGen.apiUtility.ResponseBuilder;
import com.vishalpaswan.invoiceGen.dto.responseDTO.InvoiceResponse;
import com.vishalpaswan.invoiceGen.entity.Invoice;
import com.vishalpaswan.invoiceGen.mappersUtills.InvoiceResponseMapper;
import com.vishalpaswan.invoiceGen.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {
    private final InvoiceRepository invoiceRepository;
    private final InvoiceResponseMapper invoiceResponseMapper;

    // view invoice for customers
    public ResponseEntity<?> customerInvoiceView(String invoiceId) {
        try {
            // Validate input
            if (invoiceId == null || invoiceId.isBlank()) {
                log.warn("Invalid request: invoiceId is null or blank");
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Invalid invoice ID.");
            }

            // Fetch invoice
            Optional<Invoice> invoiceOpt = invoiceRepository.findById(invoiceId);
            if (invoiceOpt.isEmpty()) {
                log.warn("Invoice not found for id={}", invoiceId);
                return ResponseBuilder.error(HttpStatus.NOT_FOUND, "Invoice not found.");
            }

            Invoice invoice = invoiceOpt.get();

            // Map invoice data to response
            InvoiceResponse invoiceResponse = invoiceResponseMapper.mapToResponse(invoice);

            // Handle potential null company safely
            if (invoice.getCompany() != null) {
                InvoiceResponse.CompanyDetails companyDetails = new InvoiceResponse.CompanyDetails();
                companyDetails.setName(invoice.getCompany().getCompanyName());
                companyDetails.setContact(invoice.getCompany().getContact());
                companyDetails.setAddress(invoice.getCompany().getAddress());
                companyDetails.setEmail(invoice.getCompany().getEmail());
                invoiceResponse.setCompanyDetails(companyDetails);
            } else {
                log.warn("Invoice {} has no associated company", invoiceId);
                invoiceResponse.setCompanyDetails(null);
            }

            log.info("Invoice {} retrieved successfully", invoiceId);
            return ResponseBuilder.success(HttpStatus.OK, "Invoice retrieved successfully", invoiceResponse);

        } catch (DataAccessException ex) {
            log.error("Database error while fetching invoice {}: {}", invoiceId, ex.getMessage(), ex);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "Database error occurred while retrieving invoice.");

        } catch (Exception ex) {
            log.error("Unexpected error while fetching invoice {}: {}", invoiceId, ex.getMessage(), ex);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.");
        }
    }
}
