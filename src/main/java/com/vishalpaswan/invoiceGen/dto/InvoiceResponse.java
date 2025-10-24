package com.vishalpaswan.invoiceGen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponse {
    private String id;
    private CompanyDetails companyDetails;
    private BillingDetails billingDetails;
    private InvoiceDetails invoiceDetails;
    private List<ItemsDetails> itemsDetails;
    private String paymentMode;
    private Integer grandTotal;
    private Integer paidAmount;
    private Integer dueBalance;
    private boolean dueClear;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CompanyDetails {
        private String name;
        private String contact;
        private String email;
        private String address;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BillingDetails {
        private String name;
        private String phone;
        private String email;
        private String address;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InvoiceDetails {
        private String invNumber;
        private String date;
        private String dueDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemsDetails {
        private String name;
        private Integer quantity;
        private Double rate;
        private String description;
    }

}
