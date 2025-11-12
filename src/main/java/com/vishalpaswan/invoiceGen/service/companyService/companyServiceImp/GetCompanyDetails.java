package com.vishalpaswan.invoiceGen.service.companyService.companyServiceImp;

import com.vishalpaswan.invoiceGen.apiUtility.ResponseBuilder;
import com.vishalpaswan.invoiceGen.dto.responseDTO.CompanyResponse;
import com.vishalpaswan.invoiceGen.entity.Companies;
import com.vishalpaswan.invoiceGen.entity.Users;
import com.vishalpaswan.invoiceGen.mappersUtills.CompanyResponseMapper;
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
public class GetCompanyDetails {
    private final CompaniesRepository companiesRepository;
    private final UserRepository userRepository;
    private final CompanyResponseMapper companyResponseMapper;

    private ResponseEntity<?> getCompanyDetails(String ownerId, String companyId) {
        try {
            // Basic validation
            if (ownerId == null || companyId == null || ownerId.isBlank() || companyId.isBlank()) {
                log.warn("Invalid request: ownerId or companyId is null/blank. ownerId={}, companyId={}", ownerId, companyId);
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Invalid request. Missing owner or company ID.");
            }

            // Fetch user and company
            Optional<Users> userOpt = userRepository.findById(ownerId);
            Optional<Companies> companyOpt = companiesRepository.findById(companyId);

            if (userOpt.isEmpty()) {
                log.warn("User not found for ID: {}", ownerId);
                return ResponseBuilder.error(HttpStatus.NOT_FOUND, "User not found.");
            }

            if (companyOpt.isEmpty()) {
                log.warn("Company not found for ID: {}", companyId);
                return ResponseBuilder.error(HttpStatus.NOT_FOUND, "Company details not found.");
            }

            Companies companyData = companyOpt.get();

            // Ensure company belongs to user
            if (!companyData.getOwner().getId().equals(ownerId)) {
                log.warn("Unauthorized access attempt: user {} tried to access company {}", ownerId, companyId);
                return ResponseBuilder.error(HttpStatus.FORBIDDEN, "You are not authorized to view this company's details.");
            }

            // Map and generate invoice number
            Users user = userOpt.get();
            CompanyResponse companyResponse = companyResponseMapper.mapToCompanyResponse(companyData);
            companyResponse.setTotalInvoice(user.getTotalInvoices());

            log.info("Company details retrieved successfully for companyId={} by user={}", companyId, ownerId);
            return ResponseBuilder.success(HttpStatus.OK, "Company details retrieved successfully.", companyResponse);

        } catch (DataAccessException ex) {
            log.error("Database error while fetching company details for user {}: {}", ownerId, ex.getMessage(), ex);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "Database error occurred while retrieving company details.");

        } catch (Exception ex) {
            log.error("Unexpected error while fetching company details for user {}: {}", ownerId, ex.getMessage(), ex);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.");
        }
    }

    public ResponseEntity<?> companyDetails(String ownerId, String companyId) {
        return getCompanyDetails(ownerId, companyId);
    }

}
