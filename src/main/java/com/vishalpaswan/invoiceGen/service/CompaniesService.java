package com.vishalpaswan.invoiceGen.service;

import com.vishalpaswan.invoiceGen.dto.AllCompanyList;
import com.vishalpaswan.invoiceGen.dto.CompanyResponse;
import com.vishalpaswan.invoiceGen.dto.NewCompanyRequest;
import com.vishalpaswan.invoiceGen.entity.Companies;
import com.vishalpaswan.invoiceGen.entity.Users;
import com.vishalpaswan.invoiceGen.inputValidationCheck.ValidateInput;
import com.vishalpaswan.invoiceGen.mappersUtills.AllCompanyMapper;
import com.vishalpaswan.invoiceGen.mappersUtills.CompanyRequestMapper;
import com.vishalpaswan.invoiceGen.mappersUtills.CompanyResponseMapper;
import com.vishalpaswan.invoiceGen.repository.CompaniesRepository;
import com.vishalpaswan.invoiceGen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompaniesService {
    private final CompaniesRepository companiesRepository;
    private final UserRepository userRepository;
    private final CompanyRequestMapper companyRequestMapper;
    private final CompanyResponseMapper companyResponseMapper;
    private final AllCompanyMapper allCompanyMapper;

    // save company data
    @Transactional
    public ResponseEntity<?> saveNewCompanyData(NewCompanyRequest companyRequest, String ownerId) {
        try {
            // Check if user exists
            Optional<Users> userExist = userRepository.findById(ownerId);
            if (userExist.isEmpty()) {
                log.warn("Attempt to add company with non-existent user ID: {}", ownerId);
                return ResponseEntity.badRequest().body("Please signup to add your company.");
            }

            Users currentUser = userExist.get();

            // Restrict multiple company creation
            if (currentUser.getTotalCompany() != 0) {
                log.warn("User {} tried to add more than one company", ownerId);
                return ResponseEntity.badRequest().body("Only one company can be added!");
            }
            // Validate email and phone number
            if (!ValidateInput.isValidEmail(companyRequest.getEmail())) {
                log.warn("Invalid email provided: {}", companyRequest.getEmail());
                return ResponseEntity.badRequest().body("Invalid email.");
            }

            if (!ValidateInput.isValidPhoneNumber(companyRequest.getContact())) {
                log.warn("Invalid phone number provided: {}", companyRequest.getContact());
                return ResponseEntity.badRequest().body("Invalid phone number.");
            }

            // Map and save company
            Companies newCompany = companyRequestMapper.mapToCompanies(companyRequest);
            newCompany.setOwner(currentUser);
            Companies savedCompany = companiesRepository.save(newCompany);

            // Update user info
            currentUser.setTotalCompany(1);
            currentUser.getCompanies().add(savedCompany);
            userRepository.save(currentUser);

            log.info("New company '{}' added successfully for user '{}'", newCompany.getCompanyName(), ownerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedCompany);

        } catch (DataAccessException ex) {
            log.error("Database error while saving company for user {}: {}", ownerId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("A database error occurred. Please try again later.");

        } catch (Exception ex) {
            log.error("Unexpected error while saving company for user {}: {}", ownerId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred. Please try again later.");
        }
    }

    // get company details
    public ResponseEntity<?> getCompanyDetails(String ownerId, String companyId) {
        try {
            // Basic validation
            if (ownerId == null || companyId == null || ownerId.isBlank() || companyId.isBlank()) {
                log.warn("Invalid request: ownerId or companyId is null/blank. ownerId={}, companyId={}", ownerId, companyId);
                return ResponseEntity.badRequest().body("Invalid request. Missing owner or company ID.");
            }

            // Fetch user and company
            Optional<Users> userOpt = userRepository.findById(ownerId);
            Optional<Companies> companyOpt = companiesRepository.findById(companyId);

            if (userOpt.isEmpty()) {
                log.warn("User not found for ID: {}", ownerId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
            }

            if (companyOpt.isEmpty()) {
                log.warn("Company not found for ID: {}", companyId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Company details not found.");
            }

            Companies companyData = companyOpt.get();

            // Ensure company belongs to user
            if (!companyData.getOwner().getId().equals(ownerId)) {
                log.warn("Unauthorized access attempt: user {} tried to access company {}", ownerId, companyId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to view this company's details.");
            }

            // Map and generate invoice number
            Users user = userOpt.get();
            CompanyResponse companyResponse = companyResponseMapper.mapToCompanyResponse(companyData);
            companyResponse.setTotalInvoice(user.getTotalInvoices());

            log.info("Company details retrieved successfully for companyId={} by user={}", companyId, ownerId);
            return ResponseEntity.ok(companyResponse);

        } catch (DataAccessException ex) {
            log.error("Database error while fetching company details for user {}: {}", ownerId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Database error occurred while retrieving company details.");

        } catch (Exception ex) {
            log.error("Unexpected error while fetching company details for user {}: {}", ownerId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred. Please try again later.");
        }
    }

    // get all company list...
    public ResponseEntity<?> getAllCompany(String ownerId) {
        try {
            // Validate ownerId
            if (ownerId == null || ownerId.isBlank()) {
                log.warn("Invalid request: ownerId is null or empty");
                return ResponseEntity.badRequest().body("Owner ID is required.");
            }

            // Fetch user
            Optional<Users> userOpt = userRepository.findById(ownerId);
            if (userOpt.isEmpty()) {
                log.warn("No user found for ownerId={}", ownerId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Owner of company not found.");
            }

            Users owner = userOpt.get();

            // Retrieve associated companies
            List<Companies> companiesList = owner.getCompanies();
            if (companiesList == null || companiesList.isEmpty()) {
                log.info("No companies found for user {}", ownerId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No company found.");
            }

            // Map to DTO/Response model
            List<AllCompanyList> companyResponses = allCompanyMapper.mapToAllCompanyList(companiesList);

            log.info("Retrieved {} companies for user {}", companyResponses.size(), ownerId);
            return ResponseEntity.ok(companyResponses);

        } catch (DataAccessException ex) {
            log.error("Database error while fetching companies for user {}: {}", ownerId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Database error occurred while fetching company list.");

        } catch (Exception ex) {
            log.error("Unexpected error while fetching companies for user {}: {}", ownerId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred. Please try again later.");
        }
    }
}
