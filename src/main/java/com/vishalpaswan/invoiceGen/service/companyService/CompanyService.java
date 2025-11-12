package com.vishalpaswan.invoiceGen.service.companyService;

import com.vishalpaswan.invoiceGen.dto.requestDTO.NewCompanyRequest;
import com.vishalpaswan.invoiceGen.service.companyService.companyServiceImp.GetAllCompany;
import com.vishalpaswan.invoiceGen.service.companyService.companyServiceImp.GetCompanyDetails;
import com.vishalpaswan.invoiceGen.service.companyService.companyServiceImp.SaveCompany;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyService {
    private final SaveCompany saveCompany;
    private final GetCompanyDetails getDetails;
    private final GetAllCompany getAll;

    // save new company
    public ResponseEntity<?> saveNewCompanyData(NewCompanyRequest companyRequest, String ownerId) {
        return saveCompany.newCompany(companyRequest, ownerId);
    }

    // get company details
    public ResponseEntity<?> getCompanyDetails(String ownerId, String companyId) {
        return getDetails.companyDetails(ownerId, companyId);
    }

    // get all company list...
    public ResponseEntity<?> getAllCompany(String ownerId) {
        return getAll.allCompany(ownerId);
    }
}
