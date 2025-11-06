package com.vishalpaswan.invoiceGen.service;

import com.vishalpaswan.invoiceGen.apiUtility.ResponseBuilder;
import com.vishalpaswan.invoiceGen.dto.responseDTO.GraphStatsDays;
import com.vishalpaswan.invoiceGen.dto.responseDTO.GraphStatsMonths;
import com.vishalpaswan.invoiceGen.dto.responseDTO.GraphStatsWeeks;
import com.vishalpaswan.invoiceGen.dto.responseDTO.StatsSummary;
import com.vishalpaswan.invoiceGen.entity.Companies;
import com.vishalpaswan.invoiceGen.entity.Invoice;
import com.vishalpaswan.invoiceGen.entity.Users;
import com.vishalpaswan.invoiceGen.inputValidationCheck.StatsDataConversion;
import com.vishalpaswan.invoiceGen.repository.InvoiceRepository;
import com.vishalpaswan.invoiceGen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;

    public boolean isOwnerAndCompanyExist(String ownerId, String companyId) {
        Optional<Users> users = userRepository.findById(ownerId);
        if (users.isEmpty()) {
            return false;
        }
        List<Companies> companiesList = users.get().getCompanies();
        for (Companies companies : companiesList) {
            if (companies.getId().equals(companyId)) {
                return true;
            }
        }
        return false;
    }

    // last 15 days
    public ResponseEntity<?> last15DaysStats(String ownerId, String companyId) {
        try {
            if (!isOwnerAndCompanyExist(ownerId, companyId)) {
                log.warn("Invalid owner/company access: ownerId={}, companyId={}", ownerId, companyId);
                return ResponseBuilder.error(HttpStatus.NOT_FOUND, "Data not found.");
            }

            LocalDate today = LocalDate.now();
            LocalDate startDate = today.minusDays(15);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            List<Invoice> allInvoices = invoiceRepository.findInvoiceDetailsBetweenDatesAndCompany(
                    startDate.format(formatter),
                    today.minusDays(1).format(formatter),
                    companyId
            );

            Map<String, List<Invoice>> groupedInvoices = allInvoices.stream()
                    .collect(Collectors.groupingBy(inv -> inv.getInvoiceDetails().getDate()));

            List<GraphStatsDays> last15Days = new ArrayList<>();

            for (int i = 0; i < 15; i++) {
                LocalDate date = startDate.plusDays(i);
                String dateKey = date.format(formatter);
                List<Invoice> dayInvoices = groupedInvoices.getOrDefault(dateKey, Collections.emptyList());

                GraphStatsDays stats = new GraphStatsDays();
                stats.setDate(StatsDataConversion.changeDateFormat(dateKey));
                stats.setAmount(dayInvoices.isEmpty() ? 0 : StatsDataConversion.findTotalAmount(dayInvoices));
                last15Days.add(stats);
            }

            log.info("Successfully fetched last 15 days stats for ownerId={}, companyId={}", ownerId, companyId);
            return ResponseBuilder.success(HttpStatus.OK, "Successfully fetched last 15 days stats", last15Days);

        } catch (DataAccessException e) {
            log.error("Database error while fetching last 15 days stats for ownerId={}, companyId={}: {}", ownerId, companyId, e.getMessage(), e);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "Database error occurred while fetching stats.");
        } catch (Exception e) {
            log.error("Unexpected error while fetching last 15 days stats for ownerId={}, companyId={}: {}", ownerId, companyId, e.getMessage(), e);
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred while generating stats.");
        }
    }


    // last 7 weeks
    public ResponseEntity<?> last7WeeksStats(String ownerId, String companyId) {
        try {
            // Validate owner and company existence
            if (!isOwnerAndCompanyExist(ownerId, companyId)) {
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

    // last 12 months
    public ResponseEntity<?> last12MonthsStats(String ownerId, String companyId) {
        try {
            // Validate if owner and company exist
            if (!isOwnerAndCompanyExist(ownerId, companyId)) {
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

    // stats summary of all invoices and profit
    public ResponseEntity<?> getStatsSummary(String ownerId, String companyId) {
        try {
            // Validate if owner and company exist
            if (!isOwnerAndCompanyExist(ownerId, companyId)) {
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
                    if (invoice.getDueBalance() == 0) {
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
}
