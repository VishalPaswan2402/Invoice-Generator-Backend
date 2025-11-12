package com.vishalpaswan.invoiceGen.service.statsService.statsServiceImp;

import com.vishalpaswan.invoiceGen.apiUtility.ResponseBuilder;
import com.vishalpaswan.invoiceGen.dto.responseDTO.GraphStatsMonths;
import com.vishalpaswan.invoiceGen.entity.Invoice;
import com.vishalpaswan.invoiceGen.inputValidationCheck.StatsDataConversion;
import com.vishalpaswan.invoiceGen.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class Last12Months {
    private final InvoiceRepository invoiceRepository;
    private final OwnerCompanyExist ownerCompanyExist;

    private ResponseEntity<?> last12MonthsStats(String ownerId, String companyId) {
        try {
            // Validate if owner and company exist
            if (!ownerCompanyExist.isOwnerAndCompany(ownerId, companyId)) {
                log.warn("Invalid request: Owner or Company not found. OwnerId={}, CompanyId={}", ownerId, companyId);
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Data not found.");
            }

            LocalDate today = LocalDate.now();
            YearMonth startMonth = YearMonth.from(today.minusMonths(11));
            DateTimeFormatter dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // Fetch invoices for the past 12 months
            List<Invoice> allInvoices = invoiceRepository.findInvoiceDetailsBetweenDatesAndCompany(
                    startMonth.atDay(1).format(dbFormatter),
                    today.format(dbFormatter),
                    companyId
            );

            // Group invoices by month and year
            Map<String, List<Invoice>> groupedByMonth = allInvoices.stream()
                    .collect(Collectors.groupingBy(inv -> {
                        LocalDate date = LocalDate.parse(inv.getInvoiceDetails().getDate(), dbFormatter);
                        return date.getYear() + "-" + date.getMonthValue();
                    }));

            List<GraphStatsMonths> last12Months = new ArrayList<>();

            // Iterate through last 12 months
            for (int i = 0; i < 12; i++) {
                YearMonth month = startMonth.plusMonths(i);
                String key = month.getYear() + "-" + month.getMonthValue();
                List<Invoice> monthInvoices = groupedByMonth.getOrDefault(key, Collections.emptyList());

                GraphStatsMonths stats = new GraphStatsMonths();
                stats.setMonth(month.getMonth().toString().substring(0, 3) + " " + month.getYear()); // e.g., "Oct 2025"
                stats.setAmount(monthInvoices.isEmpty() ? 0 : StatsDataConversion.findTotalAmount(monthInvoices));

                last12Months.add(stats);
            }

            log.info("Successfully generated last 12 months stats for companyId={}", companyId);
            return ResponseBuilder.success(HttpStatus.OK, "Successfully generated last 12 months stats", last12Months);
        } catch (DateTimeParseException e) {
            log.error("Date parsing error while generating monthly stats: {}", e.getMessage());
            return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Invalid date format in invoice data.");
        } catch (NullPointerException e) {
            log.error("Null value encountered while generating monthly stats: {}", e.getMessage());
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "Some required data is missing.");
        } catch (DataAccessException e) {
            log.error("Database error while fetching last 12 months stats for ownerId={}, companyId={}: {}", ownerId, companyId, e.getMessage(), e);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "Database error occurred while fetching stats.");
        } catch (Exception e) {
            log.error("Unexpected error occurred while generating monthly stats: {}", e.getMessage(), e);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred while processing monthly stats.");
        }
    }

    public ResponseEntity<?> last12MonthsData(String ownerId, String companyId) {
        return last12MonthsStats(ownerId, companyId);
    }
}
