package com.vishalpaswan.invoiceGen.service.statsService.statsServiceImp;

import com.vishalpaswan.invoiceGen.apiUtility.ResponseBuilder;
import com.vishalpaswan.invoiceGen.dto.responseDTO.GraphStatsDays;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class Last15Days {
    private final InvoiceRepository invoiceRepository;
    private final OwnerCompanyExist ownerCompanyExist;

    private ResponseEntity<?> last15DaysStats(String ownerId, String companyId) {
        try {
            if (!ownerCompanyExist.isOwnerAndCompany(ownerId, companyId)) {
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

    public ResponseEntity<?> last15DaysData(String ownerId, String companyId) {
        return last15DaysStats(ownerId, companyId);
    }
}
