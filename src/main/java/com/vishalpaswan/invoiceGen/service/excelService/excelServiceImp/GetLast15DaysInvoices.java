package com.vishalpaswan.invoiceGen.service.excelService.excelServiceImp;

import com.vishalpaswan.invoiceGen.entity.Invoice;
import com.vishalpaswan.invoiceGen.repository.InvoiceRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class GetLast15DaysInvoices {
    private final InvoiceRepository invoiceRepository;

    private List<Invoice> getAllInvoicesOfLast15DaysForExcel(String companyId) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(15);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<Invoice> invoices = invoiceRepository.findInvoiceDetailsBetweenDatesAndCompany(
                startDate.format(formatter),
                today.minusDays(1).format(formatter),
                companyId
        );

        if (invoices == null || invoices.isEmpty()) {
            log.error("Last 15 days invoice not found.");
            return null;
        }
        log.info("Last 15 days invoice found.");
        return invoices;
    }

    public List<Invoice> getLast15Days(String companyId) {
        return getAllInvoicesOfLast15DaysForExcel(companyId);
    }
    
}
