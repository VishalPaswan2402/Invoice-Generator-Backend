package com.vishalpaswan.invoiceGen.service;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final CompaniesRepository companiesRepository;
    private final InvoiceResponseMapper invoiceResponseMapper;
    private final InvoiceRequestMapper invoiceRequestMapper;

    // save new invoice
    @Transactional
    public ResponseEntity<?> saveNewInvoice(InvoiceRequest invoiceRequest, String companyOwnerId) {
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

    // fetch invoice
    public ResponseEntity<?> getInvoiceById(String invoiceId, String userId) {
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

    // fetch latest 20 invoice
    public ResponseEntity<?> getLatestInvoice(String ownerId) {
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

    // get all invoice
    public ResponseEntity<?> getAllInvoice(String ownerId, String companyId) {
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
            if (user.getTotalCompany() == 0 || user.getCompanies().isEmpty()) {
                log.warn("User {} has no company registered", ownerId);
                return ResponseBuilder.error(HttpStatus.CONFLICT, "No company found. Please create your company first.");
            }

            // Verify that the requested company belongs to this user
            boolean ownsCompany = user.getCompanies()
                    .stream()
                    .anyMatch(c -> c.getId().equals(companyId));

            if (!ownsCompany) {
                log.warn("Unauthorized access attempt: user {} tried to access company {}", ownerId, companyId);
                return ResponseBuilder.error(HttpStatus.FORBIDDEN, "You are not authorized to view invoices for this company.");
            }

            // Fetch invoices for the company
            List<Invoice> allInvoices = invoiceRepository.findByCompanyId(
                    companyId, Sort.by(Sort.Direction.DESC, "id")
            );

            if (allInvoices.isEmpty()) {
                log.info("No invoices found for companyId: {}", companyId);
                return ResponseBuilder.success(HttpStatus.NO_CONTENT, "No invoices available.", null);
            }

            // Map to response DTO
            List<InvoiceResponse> responseList = allInvoices.stream()
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

    // delete invoice by id
    public ResponseEntity<?> deleteInvoiceById(String invoiceId, String ownerId) {
        try {
            if (ownerId == null || ownerId.isBlank() || invoiceId == null || invoiceId.isBlank()) {
                log.warn("Invalid request: ownerId or invoiceId is null/blank");
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Invalid request. Owner ID and Invoice ID are required.");
            }
            Optional<Invoice> findInvoice = invoiceRepository.findById(invoiceId);
            if (findInvoice.isEmpty()) {
                return ResponseBuilder.error(HttpStatus.NOT_FOUND, "Invoice not found.");
            }

            String invoiceOwnerId = findInvoice.get().getCompany().getOwner().getId();
            if (!invoiceOwnerId.equals(ownerId)) {
                return ResponseBuilder.error(HttpStatus.UNAUTHORIZED, "Not allowed to delete!");
            }

            invoiceRepository.deleteById(invoiceId);
            log.info("Invoice with ID {} deleted successfully by owner {}", invoiceId, ownerId);
            return ResponseBuilder.success(HttpStatus.OK, "Invoice deleted successfully.", null);

        } catch (DataAccessException ex) {
            log.error("Database error while deleting invoice {} for owner {}: {}", invoiceId, ownerId, ex.getMessage(), ex);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "Database error occurred while deleting invoice.");

        } catch (Exception e) {
            log.error("Unexpected error while deleting invoice {} for owner {}: {}", invoiceId, ownerId, e.getMessage(), e);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred while deleting the invoice. Please try again later.");
        }
    }

    // update or edit invoice
    public ResponseEntity<?> updateInvoice(InvoiceRequest newInvoice, String ownerId, String invoiceId) {
        try {
            if (ownerId == null || ownerId.isBlank() || invoiceId == null || invoiceId.isBlank() || newInvoice == null) {
                log.warn("Invalid request: ownerId or invoiceId or newInvoice is null/blank");
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Invalid request. Owner ID , Invoice ID and New Invoice are required.");
            }
            Optional<Invoice> oldInvoice = invoiceRepository.findById(invoiceId);
            if (oldInvoice.isEmpty()) {
                return ResponseBuilder.error(HttpStatus.NOT_FOUND, "Invoice not found.");
            }

            if (newInvoice.getItemsDetails() == null || newInvoice.getItemsDetails().isEmpty()) {
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "No items found in updated invoice.");
            }

            Invoice oldDetails = oldInvoice.get();
            if (!oldDetails.getCompany().getOwner().getId().equals(ownerId)) {
                return ResponseBuilder.error(HttpStatus.UNAUTHORIZED, "Not authorized to update this invoice!");
            }

            // Recalculate totals
            int grandTotal = newInvoice.getItemsDetails().stream()
                    .mapToInt(item -> (int) (item.getQuantity() * item.getRate()))
                    .sum();
            int paidAmount = newInvoice.getPaidAmount();
            int dueBalance = grandTotal - paidAmount;

            // Map and update fields
            Invoice updatedInvoice = invoiceRequestMapper.mapToInvoice(newInvoice);
            updatedInvoice.setGrandTotal(grandTotal);
            updatedInvoice.setDueBalance(dueBalance);
            updatedInvoice.setDueClear(dueBalance == 0 || newInvoice.isDueClear());
            updatedInvoice.setCompany(oldDetails.getCompany());
            updatedInvoice.getInvoiceDetails().setInvNumber(oldDetails.getInvoiceDetails().getInvNumber());
            updatedInvoice.setId(oldDetails.getId());

            invoiceRepository.save(updatedInvoice);

            log.info("Invoice {} successfully updated by owner {}", invoiceId, ownerId);
            return ResponseBuilder.success(HttpStatus.OK, "Invoice updated successfully.", null);

        } catch (DataAccessException ex) {
            log.error("Database error while updating invoice {} for owner {}: {}", invoiceId, ownerId, ex.getMessage(), ex);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "Database error occurred while updating invoice.");

        } catch (Exception e) {
            log.error("Unexpected error while updating invoice {} for owner {}: {}", invoiceId, ownerId, e.getMessage(), e);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred while updating invoice. Please try again later.");
        }
    }
}
