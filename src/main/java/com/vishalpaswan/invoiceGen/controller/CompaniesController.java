package com.vishalpaswan.invoiceGen.controller;

import com.vishalpaswan.invoiceGen.dto.requestDTO.NewCompanyRequest;
import com.vishalpaswan.invoiceGen.security.AuthUtils;
import com.vishalpaswan.invoiceGen.security.Authorize;
import com.vishalpaswan.invoiceGen.service.CompaniesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invoice-gen/api")
@RequiredArgsConstructor
@CrossOrigin("*")
public class CompaniesController {
    
    private final CompaniesService companiesService;
    private final AuthUtils authUtils;
    private final Authorize authorize;

    // save new company
    @PostMapping("/{ownerId}/add-company")
    public ResponseEntity<?> saveNewCompany(@Valid @RequestBody NewCompanyRequest companyRequest, @PathVariable String ownerId, @RequestHeader("Authorization") String authHeader) {
        ResponseEntity<?> authResult = authorize.isAuthorizes(authHeader, ownerId);
        return authResult != null ? authResult : companiesService.saveNewCompanyData(companyRequest, ownerId);
    }

    // get company details
    @GetMapping("/{ownerId}/{companyId}/view-company")
    public ResponseEntity<?> getCompanyDetails(@PathVariable String ownerId, @PathVariable String companyId, @RequestHeader("Authorization") String authHeader) {
        ResponseEntity<?> authResult = authorize.isAuthorizes(authHeader, ownerId);
        return authResult != null ? authResult : companiesService.getCompanyDetails(ownerId, companyId);
    }

    // get all company belonging to particular user
    @GetMapping("/{ownerId}/all-company")
    public ResponseEntity<?> getAllCompany(@PathVariable String ownerId, @RequestHeader("Authorization") String authHeader) {
        ResponseEntity<?> authResult = authorize.isAuthorizes(authHeader, ownerId);
        return authResult != null ? authResult : companiesService.getAllCompany(ownerId);
    }

}
