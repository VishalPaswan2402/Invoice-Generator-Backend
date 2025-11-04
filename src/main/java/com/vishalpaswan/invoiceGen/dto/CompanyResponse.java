package com.vishalpaswan.invoiceGen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyResponse {
    private String id;
    private String companyName;
    private String ownerName;
    private String contact;
    private String address;
    private String email;
    private long totalInvoice;
}
