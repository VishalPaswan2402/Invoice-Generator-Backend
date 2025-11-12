package com.vishalpaswan.invoiceGen.controller;

import com.vishalpaswan.invoiceGen.security.Authorize;
import com.vishalpaswan.invoiceGen.service.excelService.ExcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invoice-gen/api")
@RequiredArgsConstructor
public class ExcelController {
    private final ExcelService excelService;
    private final Authorize authorize;

    @GetMapping("/{ownerId}/{companyId}/excel")
    public ResponseEntity<?> generateExcelSheet(
            @PathVariable String ownerId,
            @PathVariable String companyId,
            @RequestParam(required = true) String excelType,
            @RequestHeader("Authorization") String authHeader
    ) {
        ResponseEntity<?> authResult = authorize.isAuthorizes(authHeader, ownerId);
        return authResult != null ? authResult : excelService.getExcelFileOfAllInvoices(ownerId, companyId, excelType);
    }

}
