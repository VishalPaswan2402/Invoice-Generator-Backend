package com.vishalpaswan.invoiceGen.service;

import com.vishalpaswan.invoiceGen.entity.Companies;
import com.vishalpaswan.invoiceGen.entity.Users;
import com.vishalpaswan.invoiceGen.repository.CompaniesRepository;
import com.vishalpaswan.invoiceGen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CompaniesService {
    private final CompaniesRepository companiesRepository;
    private final UserRepository userRepository;

    // validate email
    public boolean isValidEmail(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email != null && email.matches(regex);
    }

    // validate mobile number
    public boolean isValidMobile(String mobile) {
        return mobile != null && mobile.matches("^[6-9]\\d{9}$");
    }

    //    @Transactional
    // save company data
    public ResponseEntity<?> saveNewCompanyData(Companies company, String ownerId) {
        Optional<Users> userExist = userRepository.findById(ownerId);
        if (userExist.isEmpty()) {
            return new ResponseEntity<>("Please signup to add your company.", HttpStatus.BAD_REQUEST);
        }
        Users currentUser = userExist.get();
//        if (currentUser.getTotalCompany() != 0) {
//            return new ResponseEntity<>("Only one company can be added!", HttpStatus.BAD_REQUEST);
//        }
        if (!isValidEmail(company.getEmail())) {
            return new ResponseEntity<>("Invalid email.", HttpStatus.BAD_REQUEST);
        }
        if (!isValidMobile(company.getContact())) {
            return new ResponseEntity<>("Invalid phone number.", HttpStatus.BAD_REQUEST);
        }
        company.setOwnerId(currentUser.getId());
        Companies savedCompany = companiesRepository.save(company);
//        currentUser.setTotalCompany(1);
//        currentUser.setCompany(savedCompany);
        Users saved = userRepository.save(currentUser);
        return new ResponseEntity<>(savedCompany, HttpStatus.CREATED);
    }

}
