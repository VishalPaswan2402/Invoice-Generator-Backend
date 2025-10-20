package com.vishalpaswan.invoiceGen.service;

import com.vishalpaswan.invoiceGen.dto.AllCompanyList;
import com.vishalpaswan.invoiceGen.dto.CompanyResponse;
import com.vishalpaswan.invoiceGen.dto.NewCompanyRequest;
import com.vishalpaswan.invoiceGen.entity.Companies;
import com.vishalpaswan.invoiceGen.entity.Users;
import com.vishalpaswan.invoiceGen.inputValidationCheck.GenerateInvNumber;
import com.vishalpaswan.invoiceGen.inputValidationCheck.ValidateInput;
import com.vishalpaswan.invoiceGen.mappersUtills.AllCompanyMapper;
import com.vishalpaswan.invoiceGen.mappersUtills.CompanyRequestMapper;
import com.vishalpaswan.invoiceGen.mappersUtills.CompanyResponseMapper;
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
public class CompaniesService {
    private final CompaniesRepository companiesRepository;
    private final UserRepository userRepository;
    private final CompanyRequestMapper companyRequestMapper;
    private final CompanyResponseMapper companyResponseMapper;
    private final AllCompanyMapper allCompanyMapper;

    //    @Transactional
    // save company data
    public ResponseEntity<?> saveNewCompanyData(NewCompanyRequest companyRequest, String ownerId) {
        Optional<Users> userExist = userRepository.findById(ownerId);
        if (userExist.isEmpty()) {
            return new ResponseEntity<>("Please signup to add your company.", HttpStatus.BAD_REQUEST);
        }
        Users currentUser = userExist.get();
        if (currentUser.getTotalCompany() != 0) {
            return new ResponseEntity<>("Only one company can be added!", HttpStatus.BAD_REQUEST);
        }
        if (!ValidateInput.isValidEmail(companyRequest.getEmail())) {
            return new ResponseEntity<>("Invalid email.", HttpStatus.BAD_REQUEST);
        }
        if (!ValidateInput.isValidPhoneNumber(companyRequest.getContact())) {
            return new ResponseEntity<>("Invalid phone number.", HttpStatus.BAD_REQUEST);
        }
        Companies newCompany = companyRequestMapper.mapToCompanies(companyRequest);
        newCompany.setOwner(currentUser);
        Companies savedCompany = companiesRepository.save(newCompany);
        currentUser.setTotalCompany(1);
        currentUser.getCompanies().add(savedCompany);
        Users saved = userRepository.save(currentUser);
        return new ResponseEntity<>(savedCompany, HttpStatus.CREATED);
    }

    // get company details
    public ResponseEntity<?> getCompanyDetails(String ownerId, String companyId) {
        if (ownerId == null || companyId == null) {
            return new ResponseEntity<>("Invalid request.", HttpStatus.BAD_REQUEST);
        }
        Optional<Users> user = userRepository.findById(ownerId);
        Optional<Companies> company = companiesRepository.findById(companyId);
        if (user.isEmpty() || company.isEmpty()) {
            return new ResponseEntity<>("Company details not found.", HttpStatus.BAD_REQUEST);
        }
        Companies companyData = company.get();
        if (!companyData.getOwner().getId().equals(ownerId)) {
            return new ResponseEntity<>("Company details not found.", HttpStatus.BAD_REQUEST);
        }
        CompanyResponse companyResponse = companyResponseMapper.mapToCompanyResponse(companyData);
        String newInvoiceNumber = GenerateInvNumber.generateInvoiceNumber(user.get().getTotalInvoices() + 1);
        companyResponse.setInvNumber(newInvoiceNumber);
        return new ResponseEntity<>(companyResponse, HttpStatus.OK);
    }

    // get all company list...
    public ResponseEntity<?> getAllCompany(String ownerId) {
        if (ownerId.isEmpty()) {
            return new ResponseEntity<>("Owner of company not found", HttpStatus.BAD_REQUEST);
        }
        Optional<Users> user = userRepository.findById(ownerId);
        if (user.isEmpty()) {
            return new ResponseEntity<>("Owner of company not found", HttpStatus.BAD_REQUEST);
        }
        Users ownerData = user.get();
        List<Companies> companiesList = ownerData.getCompanies();
        if (companiesList.isEmpty()) {
            System.out.println("A");
            return new ResponseEntity<>("No company found.", HttpStatus.NOT_FOUND);
        }
        List<AllCompanyList> allCompanyLists = allCompanyMapper.mapToAllCompanyList(companiesList);
        System.out.println("B");
        return new ResponseEntity<>(allCompanyLists, HttpStatus.OK);
    }
}
