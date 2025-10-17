package com.vishalpaswan.invoiceGen.controller;

import com.vishalpaswan.invoiceGen.entity.Companies;
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
    public ResponseEntity<?> saveNewCompany(@Valid @RequestBody Companies company, @PathVariable String ownerId) {
        return companiesService.saveNewCompanyData(company, ownerId);
    }

}
