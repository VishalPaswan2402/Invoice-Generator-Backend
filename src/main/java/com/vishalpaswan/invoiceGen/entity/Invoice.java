package com.vishalpaswan.invoiceGen.entity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
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

    @Valid
    @NotNull(message = "Company details are required")
    private Company company;

    @Valid
    @NotNull(message = "Billing details are required")
    private Billing billing;

    @Valid
    @NotNull(message = "Invoice details are required")
    private InvoiceDetails invoiceDetails;

    @Valid
    @NotEmpty(message = "Items list cannot be empty")
    private List<Items> items;

//    private String notes;

    @NotBlank(message = "Payment mode is required")
    private String paymentMode;

    @Min(value = 0, message = "Grand total cannot be negative")
    @NotNull(message = "Grand Total is required")
    private Integer grandTotal;

    @Min(value = 0, message = "Balance paid cannot be negative")
    @NotNull(message = "Balance Paid is required")
    private Integer paidAmount;

    @Min(value = 0, message = "Balance due cannot be negative")
    @NotNull(message = "Balance Due is required")
    private Integer dueBalance;

    @Data
    public static class Company{
        @NotBlank(message = "Company name is required")
        private String name;
        @NotBlank(message = "Company phone is required")
        private String phone;
        @NotBlank(message = "Company email is required")
        @Email(message = "Invalid company email format")
        private String email;
        @NotBlank(message = "Company address is required")
        private String address;
    }

    @Data
    public static class Billing{
        @NotBlank(message = "Billing name is required")
        private String name;
        @NotBlank(message = "Billing phone is required")
        private String phone;
        @NotBlank(message = "Company email is required")
        @Email(message = "Invalid billing email format")
        private String email;
        @NotBlank(message = "Billing address is required")
        private String address;
    }

    @Data
    public static class InvoiceDetails{
        @NotBlank(message = "Invoice number is required")
        private String invNumber;
        @NotBlank(message = "Date is required")
        private String date;
        @NotBlank(message = "Due date is required")
        private String dueDate;
    }

    @Data
    public static class Items{
        @NotBlank(message = "Item name is required")
        private String name;
        @Min(value = 1, message = "Quantity must be at least 1")
        @NotNull(message = "Quantity is required")
        private Integer quantity;
        @Min(value = 0, message = "Rate must be non-negative")
        @NotNull(message = "Rate is required")
        private Double rate;
    }
}
