package com.vishalpaswan.invoiceGen.service.excelService;

import com.vishalpaswan.invoiceGen.apiUtility.ResponseBuilder;
import com.vishalpaswan.invoiceGen.entity.Companies;
import com.vishalpaswan.invoiceGen.entity.Invoice;
import com.vishalpaswan.invoiceGen.excelHelper.DataToExcel;
import com.vishalpaswan.invoiceGen.excelHelper.ExcelDataList;
import com.vishalpaswan.invoiceGen.repository.CompaniesRepository;
import com.vishalpaswan.invoiceGen.repository.InvoiceRepository;
import com.vishalpaswan.invoiceGen.service.excelService.excelServiceImp.GetAllInvoices;
import com.vishalpaswan.invoiceGen.service.excelService.excelServiceImp.GetLast15DaysInvoices;
import com.vishalpaswan.invoiceGen.service.excelService.excelServiceImp.GetLast1YearInvoices;
import com.vishalpaswan.invoiceGen.service.excelService.excelServiceImp.GetLast7WeeksInvoices;
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
    private final GetAllInvoices getAllInvoices;
    private final GetLast15DaysInvoices getLast15DaysInvoices;
    private final GetLast7WeeksInvoices getLast7WeeksInvoices;
    private final GetLast1YearInvoices getLast1YearInvoices;

    public ResponseEntity<?> getExcelFileOfAllInvoices(String ownerId, String companyId, String excelType) {
        try {
            if (ownerId == null || ownerId.isBlank() || companyId == null || companyId.isBlank() || excelType.isBlank()) {
                log.warn("Invalid request: ownerId , companyId or excel type is null/blank");
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Invalid request. Owner ID , company ID and excel type are required.");
            }

            Optional<Companies> fetchCompany = companiesRepository.findById(companyId);
            if (fetchCompany.isEmpty()) {
                return ResponseBuilder.error(HttpStatus.NOT_FOUND, "This company not exist.");
            }
            List<Invoice> invoices = excelType.equals("all")
                    ?
                    getAllInvoices.getAllInvoice(companyId)
                    :
                    excelType.equals("15Days")
                            ?
                            getLast15DaysInvoices.getLast15Days(companyId)
                            :
                            excelType.equals("7Weeks")
                                    ?
                                    getLast7WeeksInvoices.getLast7Weeks(companyId)
                                    :
                                    excelType.equals("1Year")
                                            ?
                                            getLast1YearInvoices.getLast1Year(companyId)
                                            :
                                            null;

            if (invoices == null || invoices.isEmpty()) {
                return ResponseBuilder.error(HttpStatus.NOT_FOUND, "No invoices found for this company.");
            }

            ByteArrayInputStream byteArrayInputStream = dataToExcel.convertDataToExcel(excelDataList.makeDataList(invoices));
            String rawName = invoices.getFirst().getCompany().getCompanyName();
            String safeName = rawName.replaceAll("[^a-zA-Z0-9\\-_]", "_"); // replace invalid chars

            String fileLastName = excelType.equals("all")
                    ?
                    "All_Invoices.xlsx"
                    :
                    excelType.equals("15Days")
                            ?
                            "Last_15Days_Invoices.xlsx"
                            :
                            excelType.equals("7Weeks")
                                    ?
                                    "Last_7Weeks_Invoices.xlsx"
                                    :
                                    "Last_1Year_Invoices.xlsx";

            String fileName = safeName + fileLastName;

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
