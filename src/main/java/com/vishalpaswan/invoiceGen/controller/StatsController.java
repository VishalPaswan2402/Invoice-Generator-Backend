package com.vishalpaswan.invoiceGen.controller;

import com.vishalpaswan.invoiceGen.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invoice-gen/api")
@RequiredArgsConstructor
@CrossOrigin("*")
public class StatsController {
    private final StatsService statsService;

    @GetMapping("/{ownerId}/{companyId}/15days")
    public ResponseEntity<?> getLast15Days(@PathVariable String ownerId, @PathVariable String companyId) {
        return statsService.last15DaysStats(ownerId, companyId);
    }

    @GetMapping("/{ownerId}/{companyId}/7weeks")
    public ResponseEntity<?> getLast7Weeks(@PathVariable String ownerId, @PathVariable String companyId) {
        return statsService.last7WeeksStats(ownerId, companyId);
    }

    @GetMapping("/{ownerId}/{companyId}/12months")
    public ResponseEntity<?> getLast12Months(@PathVariable String ownerId, @PathVariable String companyId) {
        return statsService.last12MonthsStats(ownerId, companyId);
    }

    @GetMapping("/{ownerId}/{companyId}/stats-summary")
    public ResponseEntity<?> getStatsSummary(@PathVariable String ownerId, @PathVariable String companyId) {
        return statsService.getStatsSummary(ownerId, companyId);
    }
}
