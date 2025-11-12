package com.vishalpaswan.invoiceGen.service.statsService;

import com.vishalpaswan.invoiceGen.service.statsService.statsServiceImp.Last12Months;
import com.vishalpaswan.invoiceGen.service.statsService.statsServiceImp.Last15Days;
import com.vishalpaswan.invoiceGen.service.statsService.statsServiceImp.Last7Weeks;
import com.vishalpaswan.invoiceGen.service.statsService.statsServiceImp.SummaryStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {
    private final Last15Days last15Days;
    private final Last7Weeks last7Weeks;
    private final Last12Months last12Months;
    private final SummaryStats summaryStats;

    // last 15 days
    public ResponseEntity<?> last15DaysStats(String ownerId, String companyId) {
        return last15Days.last15DaysData(ownerId, companyId);
    }
    
    // last 7 weeks
    public ResponseEntity<?> last7WeeksStats(String ownerId, String companyId) {
        return last7Weeks.last7WeeksData(ownerId, companyId);
    }

    // last 12 months
    public ResponseEntity<?> last12MonthsStats(String ownerId, String companyId) {
        return last12Months.last12MonthsData(ownerId, companyId);
    }

    // stats summary of all invoices and profit
    public ResponseEntity<?> getStatsSummary(String ownerId, String companyId) {
        return summaryStats.getSummaryData(ownerId, companyId);
    }

}
