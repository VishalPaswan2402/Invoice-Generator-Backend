package com.vishalpaswan.invoiceGen.service;

import com.vishalpaswan.invoiceGen.dto.GraphStatsDays;
import com.vishalpaswan.invoiceGen.dto.GraphStatsMonths;
import com.vishalpaswan.invoiceGen.dto.GraphStatsWeeks;
import com.vishalpaswan.invoiceGen.dto.StatsSummary;
import com.vishalpaswan.invoiceGen.entity.Companies;
import com.vishalpaswan.invoiceGen.entity.Invoice;
import com.vishalpaswan.invoiceGen.entity.Users;
import com.vishalpaswan.invoiceGen.inputValidationCheck.StatsDataConversion;
import com.vishalpaswan.invoiceGen.repository.InvoiceRepository;
import com.vishalpaswan.invoiceGen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

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

    // last 30 days
    public ResponseEntity<?> last15DaysStats(String ownerId, String companyId) {
        if (!isOwnerAndCompanyExist(ownerId, companyId)) {
            return new ResponseEntity<>("Data not found.", HttpStatus.BAD_REQUEST);
        }
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(15);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//        ObjectId companyObjectId = new ObjectId(companyId);
        List<Invoice> allInvoices = invoiceRepository.findInvoiceDetailsBetweenDatesAndCompany(
                startDate.format(formatter),
                today.minusDays(1).format(formatter),
                companyId
        );
        Map<String, List<Invoice>> groupedInvoices = allInvoices.stream()
                .collect(Collectors.groupingBy(inv -> inv.getInvoiceDetails().getDate()));
        List<GraphStatsDays> last30Days = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            LocalDate date = startDate.plusDays(i);
            String dateKey = date.format(formatter);
            List<Invoice> currDayList = groupedInvoices.getOrDefault(dateKey, Collections.emptyList());
            GraphStatsDays stats = new GraphStatsDays();
            stats.setDate(StatsDataConversion.changeDateFormat(dateKey));
            stats.setAmount(currDayList.isEmpty() ? 0 : StatsDataConversion.findTotalAmount(currDayList));
            last30Days.add(stats);
        }
        return new ResponseEntity<>(last30Days, HttpStatus.OK);
    }


    // last 7 weeks
    public ResponseEntity<?> last7WeeksStats(String ownerId, String companyId) {
        if (!isOwnerAndCompanyExist(ownerId, companyId)) {
            return new ResponseEntity<>("Data not found.", HttpStatus.BAD_REQUEST);
        }
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusWeeks(7); // last 7 weeks
        DateTimeFormatter dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd MMM");

//        ObjectId companyObjectId = new ObjectId(companyId);
        List<Invoice> allInvoices = invoiceRepository.findInvoiceDetailsBetweenDatesAndCompany(
                startDate.format(dbFormatter),
                today.minusDays(1).format(dbFormatter),
                companyId
        );

        Map<Integer, List<Invoice>> groupedByWeek = allInvoices.stream()
                .collect(Collectors.groupingBy(inv -> {
                    LocalDate date = LocalDate.parse(inv.getInvoiceDetails().getDate(), dbFormatter);
                    return date.get(WeekFields.ISO.weekOfWeekBasedYear());
                }));

        List<GraphStatsWeeks> last7Weeks = new ArrayList<>();
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
        return new ResponseEntity<>(last7Weeks, HttpStatus.OK);
    }

    // last 12 months
    public ResponseEntity<?> last12MonthsStats(String ownerId, String companyId) {
        if (!isOwnerAndCompanyExist(ownerId, companyId)) {
            return new ResponseEntity<>("Data not found.", HttpStatus.BAD_REQUEST);
        }
        LocalDate today = LocalDate.now();
        YearMonth startMonth = YearMonth.from(today.minusMonths(11));
        DateTimeFormatter dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//        ObjectId companyObjectId = new ObjectId(companyId);
        List<Invoice> allInvoices = invoiceRepository.findInvoiceDetailsBetweenDatesAndCompany(
                startMonth.atDay(1).format(dbFormatter),
                today.format(dbFormatter),
                companyId
        );

        Map<String, List<Invoice>> groupedByMonth = allInvoices.stream()
                .collect(Collectors.groupingBy(inv -> {
                    LocalDate date = LocalDate.parse(inv.getInvoiceDetails().getDate(), dbFormatter);
                    return date.getYear() + "-" + date.getMonthValue();
                }));

        List<GraphStatsMonths> last12Months = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            YearMonth month = startMonth.plusMonths(i);
            String key = month.getYear() + "-" + month.getMonthValue();
            List<Invoice> monthInvoices = groupedByMonth.getOrDefault(key, Collections.emptyList());

            GraphStatsMonths stats = new GraphStatsMonths();
            stats.setMonth(month.getMonth().toString().substring(0, 3) + " " + month.getYear()); // "Oct 2025"
            stats.setAmount(monthInvoices.isEmpty() ? 0 : StatsDataConversion.findTotalAmount(monthInvoices));

            last12Months.add(stats);
        }
        return new ResponseEntity<>(last12Months, HttpStatus.OK);
    }

    public ResponseEntity<?> getStatsSummary(String ownerId, String companyId) {
        if (!isOwnerAndCompanyExist(ownerId, companyId)) {
            return new ResponseEntity<>("Data not found.", HttpStatus.BAD_REQUEST);
        }
        List<Invoice> invoiceList = invoiceRepository.findByCompanyId(companyId);
        System.out.println(invoiceList);
        int totalInvoice = invoiceList.size();
        int paidInvoice = 0;
        int unpaidInvoice = 0;
        int overDueInvoice = 0;
        Long totalSellAmount = 0L;
        for (Invoice invoice : invoiceList) {
            if (invoice.getDueBalance() == 0) {
                paidInvoice++;
            } else {
                String invDue = invoice.getInvoiceDetails().getDueDate();
                LocalDate invDueDate = LocalDate.parse(invDue);
                LocalDate today = LocalDate.now();
                if (invDueDate.isBefore(today)) {
                    overDueInvoice++;
                }
                unpaidInvoice++;
            }
            totalSellAmount += invoice.getGrandTotal();
        }
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        List<StatsSummary> statsSummaryList = List.of(
                new StatsSummary("Total Invoices", formatter.format(totalInvoice), 0),
                new StatsSummary("Paid Invoices", formatter.format(paidInvoice), 1),
                new StatsSummary("Unpaid Invoices", formatter.format(unpaidInvoice), 2),
                new StatsSummary("Overdue Invoices", formatter.format(overDueInvoice), 3),
                new StatsSummary("Total Sell", "₹ " + formatter.format(totalSellAmount), 4)
        );
        return new ResponseEntity<>(statsSummaryList, HttpStatus.OK);
    }
}
