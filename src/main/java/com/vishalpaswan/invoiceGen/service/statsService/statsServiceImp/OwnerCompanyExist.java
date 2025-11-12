package com.vishalpaswan.invoiceGen.service.statsService.statsServiceImp;

import com.vishalpaswan.invoiceGen.entity.Companies;
import com.vishalpaswan.invoiceGen.entity.Users;
import com.vishalpaswan.invoiceGen.repository.CompaniesRepository;
import com.vishalpaswan.invoiceGen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OwnerCompanyExist {
    private final UserRepository userRepository;
    private final CompaniesRepository companiesRepository;

    private boolean isOwnerAndCompanyExist(String ownerId, String companyId) {
        if (ownerId.isBlank() || companyId.isBlank()) {
            return false;
        }
        Optional<Users> users = userRepository.findById(ownerId);
        if (users.isEmpty()) {
            return false;
        }
        Optional<Companies> company = companiesRepository.findById(companyId);
        if (company.isEmpty()) {
            return false;
        }
        Companies companyDetails = company.get();
        return companyDetails.getOwner().getId().equals(ownerId);
    }

    public boolean isOwnerAndCompany(String ownerId, String companyId) {
        return isOwnerAndCompanyExist(ownerId, companyId);
    }
}
