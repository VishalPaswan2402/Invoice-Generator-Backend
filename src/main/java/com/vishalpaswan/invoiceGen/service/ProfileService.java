package com.vishalpaswan.invoiceGen.service;

import com.vishalpaswan.invoiceGen.apiUtility.ResponseBuilder;
import com.vishalpaswan.invoiceGen.dto.responseDTO.ProfileResponse;
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

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {
    private final UserRepository userRepository;
    private final CompaniesRepository companiesRepository;

    // get profile details
    public ResponseEntity<?> getProfileDetails(String ownerId) {
        try {
            if (ownerId == null || ownerId.isBlank()) {
                log.warn("Invalid request: ownerId is null/blank");
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Invalid request. Owner ID are required.");
            }
            Optional<Users> user = userRepository.findById(ownerId);
            if (user.isEmpty()) {
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "No user found.");
            }

            Users owner = user.get();
            ProfileResponse profileResponse = new ProfileResponse();
            profileResponse.setUserId(owner.getId());
            profileResponse.setUsername(owner.getUsername());
            profileResponse.setUserEmail(owner.getEmail());

            List<Companies> companiesList = companiesRepository.findAllByOwnerId(ownerId);

            if (companiesList.isEmpty()) {
                // You can decide to either return an empty list or a message
                profileResponse.getCompany().add(null);
                return ResponseBuilder.success(HttpStatus.OK, "No companies found for this user.", profileResponse);
            }

            for (Companies comp : companiesList) {
                ProfileResponse.Company currCompany = new ProfileResponse.Company(
                        comp.getId(),
                        comp.getCompanyName(),
                        comp.getOwnerName(),
                        comp.getEmail(),
                        comp.getContact(),
                        comp.getAddress(),
                        0 // invoice count placeholder
                );
                profileResponse.getCompany().add(currCompany);
            }

            return ResponseBuilder.success(HttpStatus.OK, "User profile found.", profileResponse);

        } catch (DataAccessException ex) {
            log.error("Database error while fetching profile for {}: {}", ownerId, ex.getMessage(), ex);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "Database error occurred while retrieving profile.");
        } catch (Exception e) {
            log.error("Unexpected error while fetching profile for {}: {}", ownerId, e.getMessage(), e);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.");
        }
    }
}
