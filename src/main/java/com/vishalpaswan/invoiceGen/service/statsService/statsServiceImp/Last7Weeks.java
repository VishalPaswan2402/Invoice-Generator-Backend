package com.vishalpaswan.invoiceGen.service.statsService.statsServiceImp;

import com.vishalpaswan.invoiceGen.apiUtility.ResponseBuilder;
import com.vishalpaswan.invoiceGen.dto.responseDTO.GraphStatsWeeks;
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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class Last7Weeks {
    private final InvoiceRepository invoiceRepository;
    private final OwnerCompanyExist ownerCompanyExist;

    private ResponseEntity<?> last7WeeksStats(String ownerId, String companyId) {
        try {
            // Validate owner and company existence
            if (!ownerCompanyExist.isOwnerAndCompany(ownerId, companyId)) {
                log.warn("Invalid request: Owner or Company not found. OwnerId={}, CompanyId={}", ownerId, companyId);
                return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Data not found.");
            }

            LocalDate today = LocalDate.now();
            LocalDate startDate = today.minusWeeks(7); // last 7 weeks
            DateTimeFormatter dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd MMM");

            // Fetch all invoices between date range
            List<Invoice> allInvoices = invoiceRepository.findInvoiceDetailsBetweenDatesAndCompany(
                    startDate.format(dbFormatter),
                    today.minusDays(1).format(dbFormatter),
                    companyId
            );

            // Group invoices by ISO week number
            Map<Integer, List<Invoice>> groupedByWeek = allInvoices.stream()
                    .collect(Collectors.groupingBy(inv -> {
                        LocalDate date = LocalDate.parse(inv.getInvoiceDetails().getDate(), dbFormatter);
                        return date.get(WeekFields.ISO.weekOfWeekBasedYear());
                    }));

            List<GraphStatsWeeks> last7Weeks = new ArrayList<>();

            // Build week-wise data
            for (int i = 0; i < 7; i++) {
                LocalDate weekStart = startDate.plusWeeks(i);
                LocalDate weekEnd = weekStart.plusDays(6);

                List<Invoice> weekInvoices = allInvoices.stream()
                        .filter(inv -> {
                            LocalDate invDate = LocalDate.parse(inv.getInvoiceDetails().getDate(), dbFormatter);
                            return !invDate.isBefore(weekStart) && !invDate.isAfter(weekEnd);
                        })
                        .toList();

                GraphStatsWeeks stats = new GraphStatsWeeks();
                stats.setDate("Week " + (i + 1));
                stats.setAmount(weekInvoices.isEmpty() ? 0 : StatsDataConversion.findTotalAmount(weekInvoices));
                stats.setStartDate(weekStart.format(displayFormatter));
                stats.setEndDate(weekEnd.format(displayFormatter));

                last7Weeks.add(stats);
            }

            log.info("Successfully generated last 7 weeks stats for companyId={}", companyId);
            return ResponseBuilder.success(HttpStatus.OK, "Successfully generated last 7 weeks stats", last7Weeks);
        } catch (DateTimeParseException e) {
            log.error("Date parsing error while generating weekly stats: {}", e.getMessage());
            return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Invalid date format in invoice data.");
        } catch (NullPointerException e) {
            log.error("Null value encountered while generating weekly stats: {}", e.getMessage());
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "Some required data is missing.");
        } catch (DataAccessException e) {
            log.error("Database error while fetching last 7 weeks stats for ownerId={}, companyId={}: {}", ownerId, companyId, e.getMessage(), e);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "Database error occurred while fetching stats.");
        } catch (Exception e) {
            log.error("Unexpected error occurred while generating weekly stats: {}", e.getMessage(), e);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred while processing weekly stats.");
        }
    }

    public ResponseEntity<?> last7WeeksData(String ownerId, String companyId) {
        return last7WeeksStats(ownerId, companyId);
    }
}
