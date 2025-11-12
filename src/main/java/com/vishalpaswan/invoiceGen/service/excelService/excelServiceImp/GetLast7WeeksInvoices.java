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
public class GetLast7WeeksInvoices {
    private final InvoiceRepository invoiceRepository;

    private List<Invoice> getAllInvoicesOfLast7WeeksForExcel(String companyId) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusWeeks(7); // last 7 weeks
        DateTimeFormatter dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd MMM");

        // Fetch all invoices between date range
        List<Invoice> invoices = invoiceRepository.findInvoiceDetailsBetweenDatesAndCompany(
                startDate.format(dbFormatter),
                today.minusDays(1).format(dbFormatter),
                companyId
        );

        if (invoices == null || invoices.isEmpty()) {
            log.error("Last 7 weeks invoice not found.");
            return null;
        }
        log.info("Last 7 weeks invoice found.");
        return invoices;
    }

    public List<Invoice> getLast7Weeks(String companyId) {
        return getAllInvoicesOfLast7WeeksForExcel(companyId);
    }
    
}
