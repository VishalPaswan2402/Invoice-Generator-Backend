package com.vishalpaswan.invoiceGen.service.statsService.statsServiceImp;

import com.vishalpaswan.invoiceGen.apiUtility.ResponseBuilder;
import com.vishalpaswan.invoiceGen.dto.responseDTO.StatsSummary;
import com.vishalpaswan.invoiceGen.entity.Invoice;
import com.vishalpaswan.invoiceGen.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class SummaryStats {
    private final InvoiceRepository invoiceRepository;
    private final OwnerCompanyExist ownerCompanyExist;

    private ResponseEntity<?> getStatsSummary(String ownerId, String companyId) {
        try {
            // Validate if owner and company exist
            if (!ownerCompanyExist.isOwnerAndCompany(ownerId, companyId)) {
                log.warn("Invalid request: Owner or Company not found. OwnerId={}, CompanyId={}", ownerId, companyId);
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Data not found.");
            }

            // Fetch all invoices
            List<Invoice> invoiceList = invoiceRepository.findByCompanyId(companyId);
            if (invoiceList.isEmpty()) {
                log.info("No invoices found for CompanyId={}", companyId);
                return ResponseBuilder.success(HttpStatus.NO_CONTENT, "No invoices found.", null);
            }

            int totalInvoice = invoiceList.size();
            int paidInvoice = 0;
            int unpaidInvoice = 0;
            int overDueInvoice = 0;
            long totalSellAmount = 0L;

            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // Calculate stats
            for (Invoice invoice : invoiceList) {
                try {
                    if (invoice.isDueClear() || invoice.getDueBalance() == 0) {
                        paidInvoice++;
                    } else {
                        String dueDateStr = invoice.getInvoiceDetails().getDueDate();
                        if (dueDateStr != null && !dueDateStr.isBlank()) {
                            LocalDate dueDate = LocalDate.parse(dueDateStr, formatter);
                            if (dueDate.isBefore(today)) {
                                overDueInvoice++;
                            }
                        }
                        unpaidInvoice++;
                    }
                    totalSellAmount += invoice.getGrandTotal();
                } catch (DateTimeParseException e) {
                    log.warn("Invalid date format for invoice ID={}: {}", invoice.getId(), e.getMessage());
                } catch (Exception e) {
                    log.error("Error processing invoice ID={}: {}", invoice.getId(), e.getMessage());
                }
            }

            // Format numbers for readability
            NumberFormat numberFormatter = NumberFormat.getNumberInstance(Locale.US);

            List<StatsSummary> statsSummaryList = List.of(
                    new StatsSummary("Total Invoices", numberFormatter.format(totalInvoice), 0),
                    new StatsSummary("Paid Invoices", numberFormatter.format(paidInvoice), 1),
                    new StatsSummary("Unpaid Invoices", numberFormatter.format(unpaidInvoice), 2),
                    new StatsSummary("Overdue Invoices", numberFormatter.format(overDueInvoice), 3),
                    new StatsSummary("Total Sell", "₹ " + numberFormatter.format(totalSellAmount), 4)
            );

            log.info("Successfully generated stats summary for CompanyId={}", companyId);
            return ResponseBuilder.success(HttpStatus.OK, "Successfully generated stats summary.", statsSummaryList);
        } catch (DataAccessException e) {
            log.error("Database error while fetching stats summary for CompanyId={}: {}", companyId, e.getMessage(), e);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "Database error occurred while retrieving statistics.");
        } catch (Exception e) {
            log.error("Unexpected error in getStatsSummary for CompanyId={}: {}", companyId, e.getMessage(), e);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred while generating statistics.");
        }
    }

    public ResponseEntity<?> getSummaryData(String ownerId, String companyId) {
        return getStatsSummary(ownerId, companyId);
    }

}
