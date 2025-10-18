package com.vishalpaswan.invoiceGen.controller;

import com.vishalpaswan.invoiceGen.dto.NewCompanyRequest;
import com.vishalpaswan.invoiceGen.service.CompaniesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invoice-gen/api")
@RequiredArgsConstructor
public class CompaniesController {
    private final CompaniesService companiesService;

    // save new company
    @PostMapping("/{ownerId}/add-company")
    public ResponseEntity<?> saveNewCompany(@Valid @RequestBody NewCompanyRequest companyRequest, @PathVariable String ownerId) {
        return companiesService.saveNewCompanyData(companyRequest, ownerId);
    }

    // get company details
    @GetMapping("/{ownerId}/{companyId}/view-company")
    public ResponseEntity<?> getCompanyDetails(@PathVariable String ownerId, @PathVariable String companyId) {
        return companiesService.getCompanyDetails(ownerId, companyId);
    }

}
