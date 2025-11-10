package com.vishalpaswan.invoiceGen.service;

import com.vishalpaswan.invoiceGen.apiUtility.ResponseBuilder;
import com.vishalpaswan.invoiceGen.entity.Companies;
import com.vishalpaswan.invoiceGen.entity.Invoice;
import com.vishalpaswan.invoiceGen.excelHelper.DataToExcel;
import com.vishalpaswan.invoiceGen.excelHelper.ExcelDataList;
import com.vishalpaswan.invoiceGen.repository.CompaniesRepository;
import com.vishalpaswan.invoiceGen.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelService {
    private final InvoiceRepository invoiceRepository;
    private final CompaniesRepository companiesRepository;
    private final DataToExcel dataToExcel;
    private final ExcelDataList excelDataList;

    public ResponseEntity<?> getExcelFile(String ownerId, String companyId) {
        try {
            if (ownerId == null || ownerId.isBlank() || companyId == null || companyId.isBlank()) {
                log.warn("Invalid request: ownerId and companyId is null/blank");
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Invalid request. Owner ID and company ID are required.");
            }

            Optional<Companies> fetchCompany = companiesRepository.findById(companyId);
            if (fetchCompany.isEmpty()) {
                return ResponseBuilder.error(HttpStatus.NOT_FOUND, "This company not exist.");
            }

            if (!fetchCompany.get().getOwner().getId().equals(ownerId)) {
                return ResponseBuilder.error(HttpStatus.FORBIDDEN, "Access denied for this request.");
            }

            List<Invoice> invoices = invoiceRepository.findByCompanyId(companyId);
            if (invoices == null || invoices.isEmpty()) {
                return ResponseBuilder.error(HttpStatus.NOT_FOUND, "No invoices found for this company.");
            }

            ByteArrayInputStream byteArrayInputStream = dataToExcel.convertDataToExcel(excelDataList.makeDataList(invoices));
            String rawName = invoices.getFirst().getCompany().getCompanyName();
            String safeName = rawName.replaceAll("[^a-zA-Z0-9\\-_]", "_"); // replace invalid chars
            String fileName = safeName + "_Invoices.xlsx";

            InputStreamResource file = new InputStreamResource(byteArrayInputStream);
            log.info("Excel sheet generated successfully.");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(file);

        } catch (Exception ex) {
            log.error("Internal server error while downloading excel file. : {}", ex.getMessage());
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong while downloading excel file.");
        }
    }

}
