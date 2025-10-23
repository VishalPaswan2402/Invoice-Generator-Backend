package com.vishalpaswan.invoiceGen.service;

import com.vishalpaswan.invoiceGen.dto.ProfileResponse;
import com.vishalpaswan.invoiceGen.entity.Companies;
import com.vishalpaswan.invoiceGen.entity.Users;
import com.vishalpaswan.invoiceGen.repository.CompaniesRepository;
import com.vishalpaswan.invoiceGen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final UserRepository userRepository;
    private final CompaniesRepository companiesRepository;

    public ResponseEntity<?> getProfileDetails(String ownerId) {
        Optional<Users> user = userRepository.findById(ownerId);
        if (user.isEmpty()) {
            return new ResponseEntity<>("No user found.", HttpStatus.BAD_REQUEST);
        }
        Users owner = user.get();
        ProfileResponse profileResponse = new ProfileResponse();
        profileResponse.setUserId(owner.getId());
        profileResponse.setUsername(owner.getUsername());
        profileResponse.setUserEmail(owner.getEmail());
        List<Companies> companiesList = companiesRepository.findAllByOwnerId(ownerId);
        System.out.println("List of company");
        System.out.println(companiesList);
        for (Companies comp : companiesList) {
            ProfileResponse.Company currCompany = new ProfileResponse.Company(comp.getId(), comp.getCompanyName(), comp.getOwnerName(), comp.getEmail(), comp.getContact(), comp.getAddress(), 0);
            profileResponse.getCompany().add(currCompany);
        }
        return new ResponseEntity<>(profileResponse, HttpStatus.OK);
    }

}
