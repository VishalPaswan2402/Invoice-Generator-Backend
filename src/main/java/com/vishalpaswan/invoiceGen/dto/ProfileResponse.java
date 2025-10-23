package com.vishalpaswan.invoiceGen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponse {
    private String userId;
    private String username;
    private String userEmail;
    private List<Company> company = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Company {
        private String companyId;
        private String companyName;
        private String ownerName;
        private String companyEmail;
        private String phone;
        private String getCompanyAddress;
        private long totalInvoice;
    }
}
