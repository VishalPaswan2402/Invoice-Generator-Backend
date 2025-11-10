package com.vishalpaswan.invoiceGen.controller;

import com.vishalpaswan.invoiceGen.security.Authorize;
import com.vishalpaswan.invoiceGen.service.ExcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invoice-gen/api")
@RequiredArgsConstructor
public class ExcelController {
    private final ExcelService excelService;
    private final Authorize authorize;

    @PostMapping("/{ownerId}/{companyId}/excel")
    public ResponseEntity<?> generateExcelSheet(@PathVariable String ownerId, @PathVariable String companyId, @RequestHeader("Authorization") String authHeader) {
        ResponseEntity<?> authResult = authorize.isAuthorizes(authHeader, ownerId);
        return authResult != null ? authResult : excelService.getExcelFile(ownerId, companyId);
    }

}
