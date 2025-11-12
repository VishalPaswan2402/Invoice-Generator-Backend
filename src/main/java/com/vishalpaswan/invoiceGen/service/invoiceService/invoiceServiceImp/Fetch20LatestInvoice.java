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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class Fetch20LatestInvoice {
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final InvoiceResponseMapper invoiceResponseMapper;

    private ResponseEntity<?> getLatestInvoice(String ownerId) {
        try {
            // Validate input
            if (ownerId == null || ownerId.isBlank()) {
                log.warn("Invalid request: ownerId is null or blank");
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Invalid request. Owner ID is required.");
            }

            // Fetch user
            Optional<Users> userOpt = userRepository.findById(ownerId);
            if (userOpt.isEmpty()) {
                log.warn("User not found for ownerId: {}", ownerId);
                return ResponseBuilder.error(HttpStatus.NOT_FOUND, "Account not found. Please create your account first.");
            }

            Users user = userOpt.get();

            // Check if user has a company
            if (user.getTotalCompany() == 0 || user.getCompanies().isEmpty()) {
                log.warn("User {} has no associated company", ownerId);
                return ResponseBuilder.error(HttpStatus.CONFLICT, "No company found. Please create your company first.");
            }

            // Fetch company's latest invoices
            String companyId = user.getCompanies().getFirst().getId();
            Pageable topTwenty = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id"));
            Page<Invoice> invoicePage = invoiceRepository.findByCompanyId(companyId, topTwenty);
            List<Invoice> latestInvoices = invoicePage.getContent();

            if (latestInvoices.isEmpty()) {
                log.info("No invoices found for companyId: {}", companyId);
                return ResponseBuilder.success(HttpStatus.NO_CONTENT, "No invoices available.", null);
            }

            // Map invoices to response objects
            List<InvoiceResponse> responseList = latestInvoices.stream()
                    .map(invoiceResponseMapper::mapToResponse)
                    .toList();

            log.info("Fetched {} latest invoices for company {}", responseList.size(), companyId);
            return ResponseBuilder.success(HttpStatus.OK, "Fetched latest invoices", responseList);

        } catch (DataAccessException ex) {
            log.error("Database error while fetching invoice for  {}: {}", ownerId, ex.getMessage(), ex);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "Database error occurred while retrieving invoice.");

        } catch (Exception e) {
            log.error("Error while fetching latest invoices for owner {}: {}", ownerId, e.getMessage(), e);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred while fetching invoices. Please try again later.");
        }
    }

    public ResponseEntity<?> latestInvoice(String ownerId) {
        return getLatestInvoice(ownerId);
    }

}
