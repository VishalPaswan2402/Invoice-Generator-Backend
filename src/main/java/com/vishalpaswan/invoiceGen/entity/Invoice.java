package com.vishalpaswan.invoiceGen.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection="invoices")
public class Invoice {
    @Id
    private String id;
    private Company company;
    private Billing billing;
    private InvoiceDetails invoiceDetails;
    private List<Items> items;
    private String notes;
    private String paymentMode;
    private int grandTotal;
    private int paidAmount;
    private int dueBalance;

    @Data
    public static class Company{
        private String name;
        private String phone;
        private String email;
        private String address;
    }

    @Data
    public static class Billing{
        private String name;
        private String phone;
        private String email;
        private String address;
    }

    @Data
    public static class InvoiceDetails{
        private String invNumber;
        private String date;
        private String dueDate;
    }

    @Data
    public static class Items{
        private String name;
        private int quantity;
        private double rate;
    }
}
