package com.vishalpaswan.invoiceGen.service.companyService.companyServiceImp;

import com.vishalpaswan.invoiceGen.apiUtility.ResponseBuilder;
import com.vishalpaswan.invoiceGen.dto.requestDTO.NewCompanyRequest;
import com.vishalpaswan.invoiceGen.entity.Companies;
import com.vishalpaswan.invoiceGen.entity.Users;
import com.vishalpaswan.invoiceGen.inputValidationCheck.ValidateInput;
import com.vishalpaswan.invoiceGen.mappersUtills.CompanyRequestMapper;
import com.vishalpaswan.invoiceGen.repository.CompaniesRepository;
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
public class SaveCompany {
    private final CompaniesRepository companiesRepository;
    private final UserRepository userRepository;
    private final CompanyRequestMapper companyRequestMapper;

    @Transactional
    private ResponseEntity<?> saveNewCompanyData(NewCompanyRequest companyRequest, String ownerId) {
        try {
            // Check if user exists
            Optional<Users> userExist = userRepository.findById(ownerId);
            if (userExist.isEmpty()) {
                log.warn("Attempt to add company with non-existent user ID: {}", ownerId);
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Please signup to add your company.");
            }

            Users currentUser = userExist.get();

            // Restrict multiple company creation
            if (currentUser.getTotalCompany() != 0) {
                log.warn("User {} tried to add more than one company", ownerId);
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Only one company can be added!");
            }

            if (!ValidateInput.isValidPhoneNumber(companyRequest.getContact())) {
                log.warn("Invalid phone number provided: {}", companyRequest.getContact());
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Invalid phone number.");
            }

            // Map and save company
            Companies newCompany = companyRequestMapper.mapToCompanies(companyRequest);
            newCompany.setOwner(currentUser);
            Companies savedCompany = companiesRepository.save(newCompany);

            // Update user info
            currentUser.setTotalCompany(1);
            currentUser.setCompanies(savedCompany);
            userRepository.save(currentUser);

            log.info("New company '{}' added successfully for user '{}'", newCompany.getCompanyName(), ownerId);
            return ResponseBuilder.success(HttpStatus.CREATED, "New company added successfully.", savedCompany);

        } catch (DataAccessException ex) {
            log.error("Database error while saving company for user {}: {}", ownerId, ex.getMessage(), ex);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "A database error occurred. Please try again later.");

        } catch (Exception ex) {
            log.error("Unexpected error while saving company for user {}: {}", ownerId, ex.getMessage(), ex);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.");
        }
    }

    public ResponseEntity<?> newCompany(NewCompanyRequest companyRequest, String ownerId) {
        return saveNewCompanyData(companyRequest, ownerId);
    }

}
