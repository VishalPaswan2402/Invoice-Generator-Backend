package com.vishalpaswan.invoiceGen.service.invoiceService.invoiceServiceImp;

import com.vishalpaswan.invoiceGen.apiUtility.ResponseBuilder;
import com.vishalpaswan.invoiceGen.dto.requestDTO.InvoiceRequest;
import com.vishalpaswan.invoiceGen.dto.responseDTO.InvoiceResponse;
import com.vishalpaswan.invoiceGen.entity.Companies;
import com.vishalpaswan.invoiceGen.entity.Invoice;
import com.vishalpaswan.invoiceGen.entity.Users;
import com.vishalpaswan.invoiceGen.inputValidationCheck.GenerateInvNumber;
import com.vishalpaswan.invoiceGen.mappersUtills.InvoiceRequestMapper;
import com.vishalpaswan.invoiceGen.mappersUtills.InvoiceResponseMapper;
import com.vishalpaswan.invoiceGen.repository.CompaniesRepository;
import com.vishalpaswan.invoiceGen.repository.InvoiceRepository;
import com.vishalpaswan.invoiceGen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaveInvoice {
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final CompaniesRepository companiesRepository;
    private final InvoiceResponseMapper invoiceResponseMapper;
    private final InvoiceRequestMapper invoiceRequestMapper;

    @Transactional
    private ResponseEntity<?> saveNewInvoice(InvoiceRequest invoiceRequest, String companyOwnerId) {
        try {
            // Validate input
            if (invoiceRequest == null || companyOwnerId == null || companyOwnerId.isBlank()) {
                log.warn("Invalid invoice request or missing company owner ID.");
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Invalid request data.");
            }

            // Fetch company owner
            Optional<Users> ownerOpt = userRepository.findById(companyOwnerId);
            if (ownerOpt.isEmpty()) {
                log.warn("User not found for ID: {}", companyOwnerId);
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Please create your account first.");
            }

            Users owner = ownerOpt.get();

            // Fetch associated company
            Optional<Companies> companyOpt = companiesRepository.findByOwnerId(companyOwnerId);
            if (companyOpt.isEmpty()) {
                log.warn("No company found for user {}", companyOwnerId);
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Please add your company details first.");
            }

            Companies company = companyOpt.get();

            // Generate invoice details
            int newInvoiceCount = owner.getTotalInvoices() + 1;
            String newInvoiceNumber = GenerateInvNumber.generateInvoiceNumber(newInvoiceCount);

            int grandTotal = invoiceRequest.getItemsDetails().stream()
                    .mapToInt(item -> (int) (item.getQuantity() * item.getRate()))
                    .sum();

            int paidAmount = invoiceRequest.getPaidAmount();
            int dueBalance = Math.max(0, grandTotal - paidAmount);

            // Map to entity
            Invoice invoice = invoiceRequestMapper.mapToInvoice(invoiceRequest);
            invoice.setCompany(company);
            invoice.setGrandTotal(grandTotal);
            invoice.setDueBalance(dueBalance);
            invoice.setDueClear(dueBalance == 0 || invoiceRequest.isDueClear());
            invoice.getInvoiceDetails().setInvNumber(newInvoiceNumber);

            // Save invoice
            Invoice savedInvoice = invoiceRepository.save(invoice);

            // Update owner invoice count
            owner.setTotalInvoices(newInvoiceCount);
            userRepository.save(owner);

            // Prepare response
            InvoiceResponse invoiceResponse = invoiceResponseMapper.mapToResponse(savedInvoice);
            InvoiceResponse.CompanyDetails companyDetails = new InvoiceResponse.CompanyDetails();
            companyDetails.setName(company.getCompanyName());
            companyDetails.setContact(company.getContact());
            companyDetails.setEmail(company.getEmail());
            companyDetails.setAddress(company.getAddress());
            invoiceResponse.setCompanyDetails(companyDetails);

            log.info("Invoice {} created successfully for companyOwnerId={}", newInvoiceNumber, companyOwnerId);
            return ResponseBuilder.success(HttpStatus.CREATED, "Invoice created successfully", invoiceResponse);

        } catch (DataAccessException ex) {
            log.error("Database error while saving invoice for user {}: {}", companyOwnerId, ex.getMessage(), ex);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "Database error occurred while saving invoice.");

        } catch (Exception ex) {
            log.error("Unexpected error while saving invoice for user {}: {}", companyOwnerId, ex.getMessage(), ex);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.");
        }
    }

    public ResponseEntity<?> newInvoice(InvoiceRequest invoiceRequest, String companyOwnerId) {
        return saveNewInvoice(invoiceRequest, companyOwnerId);
    }

}
