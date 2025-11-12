package com.vishalpaswan.invoiceGen.service.excelService.excelServiceImp;

import com.vishalpaswan.invoiceGen.entity.Invoice;
import com.vishalpaswan.invoiceGen.repository.InvoiceRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class GetLast1YearInvoices {
    private final InvoiceRepository invoiceRepository;

    private List<Invoice> getAllInvoicesOfLast1YearForExcel(String companyId) {
        LocalDate today = LocalDate.now();
        YearMonth startMonth = YearMonth.from(today.minusMonths(11));
        DateTimeFormatter dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Fetch invoices for the past 12 months
        List<Invoice> invoices = invoiceRepository.findInvoiceDetailsBetweenDatesAndCompany(
                startMonth.atDay(1).format(dbFormatter),
                today.format(dbFormatter),
                companyId
        );

        if (invoices == null || invoices.isEmpty()) {
            log.error("Last 1 year invoice not found.");
            return null;
        }
        log.info("Last 1 year invoice found.");
        return invoices;
    }

    public List<Invoice> getLast1Year(String companyId) {
        return getAllInvoicesOfLast1YearForExcel(companyId);
    }
    
}
