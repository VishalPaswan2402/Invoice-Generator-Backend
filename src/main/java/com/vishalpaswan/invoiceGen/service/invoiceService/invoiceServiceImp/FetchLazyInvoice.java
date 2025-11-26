package com.vishalpaswan.invoiceGen.service.invoiceService.invoiceServiceImp;

import com.vishalpaswan.invoiceGen.apiUtility.ResponseBuilder;
import com.vishalpaswan.invoiceGen.dto.responseDTO.InvoiceResponse;
import com.vishalpaswan.invoiceGen.entity.Invoice;
import com.vishalpaswan.invoiceGen.entity.Users;
import com.vishalpaswan.invoiceGen.mappersUtills.InvoiceResponseMapper;
import com.vishalpaswan.invoiceGen.repository.InvoiceRepository;
import com.vishalpaswan.invoiceGen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FetchLazyInvoice {
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final InvoiceResponseMapper invoiceResponseMapper;

    private ResponseEntity<?> lazyFetchOfInvoice(String ownerId, String companyId, int page) {
        try {
            // Input validation
            if (ownerId == null || ownerId.isBlank() || companyId == null || companyId.isBlank()) {
                log.warn("Invalid request: ownerId or companyId is null/blank");
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Invalid request. Owner ID and Company ID are required.");
            }

            // Fetch user
            Optional<Users> userOpt = userRepository.findById(ownerId);
            if (userOpt.isEmpty()) {
                log.warn("User not found for ownerId: {}", ownerId);
                return ResponseBuilder.error(HttpStatus.NOT_FOUND, "Account not found. Please create your account first.");
            }

            Users user = userOpt.get();

            // Check if user has company data
            if (user.getTotalCompany() == 0 || user.getCompanies() == null) {
                log.warn("User {} has no company registered", ownerId);
                return ResponseBuilder.error(HttpStatus.CONFLICT, "No company found. Please create your company first.");
            }

            // Verify that the requested company belongs to this user
            boolean ownsCompany = user.getCompanies().getId().equals(companyId);
            if (!ownsCompany) {
                log.warn("Unauthorized access attempt: user {} tried to access company {}", ownerId, companyId);
                return ResponseBuilder.error(HttpStatus.FORBIDDEN, "You are not authorized to view invoices for this company.");
            }

            // Fetch invoices for the company
            int size = 20;
            PageRequest pageable = PageRequest.of(page, size, Sort.by("id").descending());
            Page<Invoice> fetchedInvoices = invoiceRepository.findAll(pageable);

            // convert to response
            List<InvoiceResponse> invoiceResult = fetchedInvoices.getContent().stream()
                    .map(invoiceResponseMapper::mapToResponse)
                    .toList();

            return ResponseBuilder.success(
                    HttpStatus.OK,
                    "Your all company invoices.",
                    invoiceResult,
                    fetchedInvoices.getNumber(),
                    fetchedInvoices.getSize(),
                    fetchedInvoices.getTotalPages(),
                    fetchedInvoices.getTotalElements()
            );

        } catch (DataAccessException ex) {
            log.error("Database error while fetching invoices for owner {} and company {}: {}", ownerId, companyId, ex.getMessage(), ex);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "Database error occurred while retrieving invoices.");
        } catch (Exception e) {
            log.error("Unexpected error while fetching invoices for owner {} and company {}: {}", ownerId, companyId, e.getMessage(), e);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred while fetching invoices. Please try again later.");
        }
    }

    public ResponseEntity<?> getLazyInvoiceFetch(String ownerId, String companyId, int page) {
        return lazyFetchOfInvoice(ownerId, companyId, page);
    }

}
