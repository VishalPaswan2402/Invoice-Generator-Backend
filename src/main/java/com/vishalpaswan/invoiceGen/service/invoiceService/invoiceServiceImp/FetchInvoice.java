package com.vishalpaswan.invoiceGen.service.invoiceService.invoiceServiceImp;

import com.vishalpaswan.invoiceGen.apiUtility.ResponseBuilder;
import com.vishalpaswan.invoiceGen.dto.responseDTO.InvoiceResponse;
import com.vishalpaswan.invoiceGen.entity.Companies;
import com.vishalpaswan.invoiceGen.entity.Invoice;
import com.vishalpaswan.invoiceGen.entity.Users;
import com.vishalpaswan.invoiceGen.mappersUtills.InvoiceResponseMapper;
import com.vishalpaswan.invoiceGen.repository.InvoiceRepository;
import com.vishalpaswan.invoiceGen.repository.UserRepository;
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
public class FetchInvoice {
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final InvoiceResponseMapper invoiceResponseMapper;

    private ResponseEntity<?> getInvoiceById(String invoiceId, String userId) {
        try {
            // Validate input
            if (invoiceId == null || userId == null || invoiceId.isBlank() || userId.isBlank()) {
                log.warn("Invalid request: invoiceId or userId is null/blank");
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Invalid request. Invoice ID and User ID are required.");
            }

            // Fetch invoice
            Optional<Invoice> toFetchInvoice = invoiceRepository.findById(invoiceId);
            if (toFetchInvoice.isEmpty()) {
                log.warn("Invoice not found for ID: {}", invoiceId);
                return ResponseBuilder.error(HttpStatus.NOT_FOUND, "Invoice not found!");
            }

            Invoice invoiceDetails = toFetchInvoice.get();
            Companies company = invoiceDetails.getCompany();

            if (company == null) {
                log.error("Invoice {} has no associated company", invoiceId);
                return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "Invoice data is corrupted. Company details missing.");
            }

            // Fetch user
            Optional<Users> user = userRepository.findById(userId);
            if (user.isEmpty()) {
                log.warn("User not found for ID: {}", userId);
                return ResponseBuilder.error(HttpStatus.NOT_FOUND, "User not found!");
            }

            // Validate ownership
            boolean ownsCompany = user.get().getCompanies()
                    .stream()
                    .anyMatch(c -> c.getId().equals(company.getId()));

            if (!ownsCompany) {
                log.warn("Unauthorized access attempt by user {} for invoice {}", userId, invoiceId);
                return ResponseBuilder.error(HttpStatus.FORBIDDEN, "You are not authorized to view this invoice.");
            }

            // Map to response DTO
            InvoiceResponse invoiceResponse = invoiceResponseMapper.mapToResponse(invoiceDetails);
            InvoiceResponse.CompanyDetails companyDetails = new InvoiceResponse.CompanyDetails();
            companyDetails.setName(company.getCompanyName());
            companyDetails.setEmail(company.getEmail());
            companyDetails.setAddress(company.getAddress());
            companyDetails.setContact(company.getContact());
            invoiceResponse.setCompanyDetails(companyDetails);

            log.info("Invoice {} fetched successfully by user {}", invoiceId, userId);
            return ResponseBuilder.success(HttpStatus.OK, "Invoice fetched successfully.", invoiceResponse);

        } catch (DataAccessException ex) {
            log.error("Database error while fetching invoice {}: {}", invoiceId, ex.getMessage(), ex);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "Database error occurred while retrieving invoice.");

        } catch (Exception ex) {
            log.error("Error occurred while fetching invoice {} for user {}: {}", invoiceId, userId, ex.getMessage(), ex);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred while fetching the invoice. Please try again later.");
        }
    }

    public ResponseEntity<?> getInvoice(String invoiceId, String userId) {
        return getInvoiceById(invoiceId, userId);
    }

}
