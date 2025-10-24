package com.vishalpaswan.invoiceGen.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "invoices")
public class Invoice {
    @Id
    private String id;
    private BillingDetails billingDetails;
    private InvoiceDetails invoiceDetails;
    private List<ItemsDetails> itemsDetails;
    private String paymentMode;
    private Integer grandTotal;
    private Integer paidAmount;
    private Integer dueBalance;
    private boolean dueClear;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class BillingDetails {
        private String name;
        private String phone;
        private String email;
        private String address;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class InvoiceDetails {
        private String invNumber;
        private String date;
        private String dueDate;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ItemsDetails {
        private String name;
        private Integer quantity;
        private Double rate;
        private String description;
    }

    @DBRef(lazy = true)
    @JsonIgnore
    @ToString.Exclude
    private Companies company;

}
