package com.vishalpaswan.invoiceGen.controller;

import com.vishalpaswan.invoiceGen.security.AuthUtils;
import com.vishalpaswan.invoiceGen.security.Authorize;
import com.vishalpaswan.invoiceGen.service.statsService.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invoice-gen/api")
@RequiredArgsConstructor
@CrossOrigin("*")
public class StatsController {
    private final StatsService statsService;
    private final AuthUtils authUtils;
    private final Authorize authorize;

    @GetMapping("/{ownerId}/{companyId}/15days")
    public ResponseEntity<?> getLast15Days(
            @PathVariable String ownerId,
            @PathVariable String companyId,
            @RequestHeader("Authorization") String authHeader
    ) {
        ResponseEntity<?> authResult = authorize.isAuthorizes(authHeader, ownerId);
        return authResult != null ? authResult : statsService.last15DaysStats(ownerId, companyId);
    }

    @GetMapping("/{ownerId}/{companyId}/7weeks")
    public ResponseEntity<?> getLast7Weeks(
            @PathVariable String ownerId,
            @PathVariable String companyId,
            @RequestHeader("Authorization") String authHeader
    ) {
        ResponseEntity<?> authResult = authorize.isAuthorizes(authHeader, ownerId);
        return authResult != null ? authResult : statsService.last7WeeksStats(ownerId, companyId);
    }

    @GetMapping("/{ownerId}/{companyId}/12months")
    public ResponseEntity<?> getLast12Months(
            @PathVariable String ownerId,
            @PathVariable String companyId,
            @RequestHeader("Authorization") String authHeader
    ) {
        ResponseEntity<?> authResult = authorize.isAuthorizes(authHeader, ownerId);
        return authResult != null ? authResult : statsService.last12MonthsStats(ownerId, companyId);
    }

    @GetMapping("/{ownerId}/{companyId}/stats-summary")
    public ResponseEntity<?> getStatsSummary(
            @PathVariable String ownerId,
            @PathVariable String companyId,
            @RequestHeader("Authorization") String authHeader
    ) {
        ResponseEntity<?> authResult = authorize.isAuthorizes(authHeader, ownerId);
        return authResult != null ? authResult : statsService.getStatsSummary(ownerId, companyId);
    }

}
