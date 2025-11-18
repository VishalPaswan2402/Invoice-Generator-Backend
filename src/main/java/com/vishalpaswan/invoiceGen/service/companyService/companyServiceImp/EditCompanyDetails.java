package com.vishalpaswan.invoiceGen.service.companyService.companyServiceImp;

import com.vishalpaswan.invoiceGen.apiUtility.ResponseBuilder;
import com.vishalpaswan.invoiceGen.dto.requestDTO.EditCompanyRequest;
import com.vishalpaswan.invoiceGen.entity.Companies;
import com.vishalpaswan.invoiceGen.entity.Users;
import com.vishalpaswan.invoiceGen.repository.CompaniesRepository;
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
public class EditCompanyDetails {
    private final CompaniesRepository companiesRepository;
    private final UserRepository userRepository;

    private ResponseEntity<?> editCompanyDetails(EditCompanyRequest companyRequest, String ownerId, String companyId) {
        try {
            if (ownerId.isBlank() || companyId.isBlank()) {
                log.warn("Invalid request: ownerId and companyId is null or empty");
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Owner ID and Company ID is required.");
            }

            Optional<Users> users = userRepository.findById(ownerId);
            if (users.isEmpty()) {
                log.info("No user found for user {}", ownerId);
                return ResponseBuilder.error(HttpStatus.NOT_FOUND, "User not found.");
            }

            Optional<Companies> companies = companiesRepository.findById(companyId);

            if (companies.isEmpty()) {
                log.info("No companies found for companyId {}", companyId);
                return ResponseBuilder.error(HttpStatus.NOT_FOUND, "No company found.");
            }

            Companies company = companies.get();
            String companyOwnerId = company.getOwner().getId();

            if (!companyOwnerId.equals(ownerId)) {
                log.warn("Unauthorized access attempt: user {} tried to access company {}", ownerId, companyId);
                return ResponseBuilder.error(HttpStatus.FORBIDDEN, "You are not authorized to edit this company's details.");
            }

            company.setOwnerName(companyRequest.getOwnerName());
            company.setEmail(companyRequest.getEmail());
            company.setContact(companyRequest.getContact());
            company.setAddress(companyRequest.getAddress());
            company.setId(companyId);

            Companies updatedCompany = companiesRepository.save(company);
            
            log.info("Company '{}' details updated successfully for user '{}'", updatedCompany.getCompanyName(), ownerId);
            return ResponseBuilder.success(HttpStatus.OK, "Company details updated successfully.", updatedCompany);

        } catch (DataAccessException ex) {
            log.error("Database error while updating company for user {}: {}", ownerId, ex.getMessage(), ex);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "A database error occurred. Please try again later.");

        } catch (Exception ex) {
            log.error("Unexpected error while updating company for user {}: {}", ownerId, ex.getMessage(), ex);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.");
        }
    }

    public ResponseEntity<?> editCompany(EditCompanyRequest editCompanyRequest, String ownerId, String companyId) {
        return editCompanyDetails(editCompanyRequest, ownerId, companyId);
    }

}
