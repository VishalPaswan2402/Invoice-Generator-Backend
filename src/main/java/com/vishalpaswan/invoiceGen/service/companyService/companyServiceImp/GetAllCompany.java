package com.vishalpaswan.invoiceGen.service.companyService.companyServiceImp;

import com.vishalpaswan.invoiceGen.apiUtility.ResponseBuilder;
import com.vishalpaswan.invoiceGen.dto.responseDTO.AllCompanyList;
import com.vishalpaswan.invoiceGen.entity.Companies;
import com.vishalpaswan.invoiceGen.entity.Users;
import com.vishalpaswan.invoiceGen.mappersUtills.AllCompanyMapper;
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
public class GetAllCompany {
    private final UserRepository userRepository;
    private final AllCompanyMapper allCompanyMapper;

    private ResponseEntity<?> getAllCompany(String ownerId) {
        try {
            // Validate ownerId
            if (ownerId == null || ownerId.isBlank()) {
                log.warn("Invalid request: ownerId is null or empty");
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Owner ID is required.");
            }

            // Fetch user
            Optional<Users> userOpt = userRepository.findById(ownerId);
            if (userOpt.isEmpty()) {
                log.warn("No user found for ownerId={}", ownerId);
                return ResponseBuilder.error(HttpStatus.NOT_FOUND, "Owner of company not found.");
            }

            Users owner = userOpt.get();

            // Retrieve associated companies
            Companies companiesList = owner.getCompanies();
            if (companiesList == null) {
                log.info("No companies found for user {}", ownerId);
                return ResponseBuilder.error(HttpStatus.NOT_FOUND, "No company found.");
            }

            // Map to DTO/Response model
            AllCompanyList companyResponses = allCompanyMapper.mapToAllCompanyList(companiesList);

            log.info("Retrieved companies data for user {}", ownerId);
            return ResponseBuilder.success(HttpStatus.OK, "Retrieved companies data.", companyResponses);

        } catch (DataAccessException ex) {
            log.error("Database error while fetching companies for user {}: {}", ownerId, ex.getMessage(), ex);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "Database error occurred while fetching company list.");

        } catch (Exception ex) {
            log.error("Unexpected error while fetching companies for user {}: {}", ownerId, ex.getMessage(), ex);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.");
        }
    }

    public ResponseEntity<?> allCompany(String ownerId) {
        return getAllCompany(ownerId);
    }

}
