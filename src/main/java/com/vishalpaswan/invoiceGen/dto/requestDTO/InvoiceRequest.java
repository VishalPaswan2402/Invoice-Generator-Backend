package com.vishalpaswan.invoiceGen.dto.requestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceRequest {
    @Valid
    @NotNull(message = "Billing details are required")
    private BillingDetails billingDetails;

    @Valid
    @NotNull(message = "Invoice details are required")
    private InvoiceDetails invoiceDetails;

    @Valid
    @NotNull(message = "Items cannot be empty")
    private List<ItemsDetails> itemsDetails;

    @Valid
    @NotNull(message = "Payment status is missing.")
    private boolean dueClear;

    @Valid
    @NotBlank(message = "Payment mode is required")
    private String paymentMode;


    @Valid
    @NotNull(message = "Paid amount is required")
    @Min(value = 0, message = "Paid amount cannot be negative")
    private Integer paidAmount;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
//    @Builder
    public static class BillingDetails {
        @NotBlank(message = "Billing name is required")
        private String name;

        @NotBlank(message = "Billing phone is required")
        private String phone;

        @NotBlank(message = "Billing email is required")
        private String email;

        @NotBlank(message = "Billing address is required")
        private String address;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class InvoiceDetails {
        //        private String invNumber; // generated automatically
        @NotBlank(message = "Date is required")
        private String date;

        @NotBlank(message = "Due date is required")
        private String dueDate;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ItemsDetails {
        @NotBlank(message = "Item name is required")
        private String name;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;

        @NotNull(message = "Rate is required")
        @Min(value = 0, message = "Rate cannot be negative")
        private Double rate;

        private String description;
    }
}
