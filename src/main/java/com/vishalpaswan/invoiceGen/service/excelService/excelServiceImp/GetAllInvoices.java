package com.vishalpaswan.invoiceGen.service.excelService.excelServiceImp;

import com.vishalpaswan.invoiceGen.entity.Invoice;
import com.vishalpaswan.invoiceGen.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetAllInvoices {
    private final InvoiceRepository invoiceRepository;

    private List<Invoice> getAllInvoiceForExcel(String companyId) {
        List<Invoice> invoices = invoiceRepository.findByCompanyId(companyId);

        if (invoices == null || invoices.isEmpty()) {
            log.error("No invoice found.");
            return null;
        }

        log.info("All invoice found.");
        return invoices;
    }

    public List<Invoice> getAllInvoice(String companyId) {
        return getAllInvoiceForExcel(companyId);
    }

}
