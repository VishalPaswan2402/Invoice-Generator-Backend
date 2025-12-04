package com.vishalpaswan.invoiceGen.service.invoiceService.invoiceServiceImp;

import com.vishalpaswan.invoiceGen.apiUtility.ResponseBuilder;
import com.vishalpaswan.invoiceGen.dto.responseDTO.InvoiceResponse;
import com.vishalpaswan.invoiceGen.entity.Invoice;
import com.vishalpaswan.invoiceGen.entity.Users;
import com.vishalpaswan.invoiceGen.mappersUtills.InvoiceResponseMapper;
import com.vishalpaswan.invoiceGen.repository.CompaniesRepository;
import com.vishalpaswan.invoiceGen.repository.InvoiceRepository;
import com.vishalpaswan.invoiceGen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchInvoice {
    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;
    private final CompaniesRepository companiesRepository;
    private final InvoiceResponseMapper invoiceResponseMapper;

    private ResponseEntity<?> searchFromAllInvoice(String searchQuery, String ownerId, String companyId) {
        try {
            if (searchQuery.isBlank() || ownerId.isBlank() || companyId.isBlank()) {
                log.warn("Invalid request: ownerId, query or companyId is null/blank");
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Invalid request. Owner ID and Company ID and search query are required.");
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
            List<Invoice> searchedInvoices = invoiceRepository.searchInvoicesByQuery(new ObjectId(companyId), searchQuery);

            if (searchedInvoices.isEmpty()) {
                log.info("No invoices found for companyId: {}", companyId);
                return ResponseBuilder.success(HttpStatus.NO_CONTENT, "No invoices available.", null);
            }

            // Map to response DTO
            List<InvoiceResponse> responseList = searchedInvoices.stream()
                    .map(invoiceResponseMapper::mapToResponse)
                    .toList();

            log.info("Fetched {} invoices for company {} by user {}", responseList.size(), companyId, ownerId);
            return ResponseBuilder.success(HttpStatus.OK, "Fetched invoices", responseList);

        } catch (DataAccessException ex) {
            log.error("Database error while fetching invoices for owner {} and company {}: {}", ownerId, companyId, ex.getMessage(), ex);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "Database error occurred while retrieving invoices.");
        } catch (Exception e) {
            log.error("Unexpected error while fetching invoices for owner {} and company {}: {}", ownerId, companyId, e.getMessage(), e);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred while fetching invoices. Please try again later.");
        }
    }

    public ResponseEntity<?> searchInvoiceByQuery(String searchQuery, String ownerId, String companyId) {
        return searchFromAllInvoice(searchQuery, ownerId, companyId);
    }

}
