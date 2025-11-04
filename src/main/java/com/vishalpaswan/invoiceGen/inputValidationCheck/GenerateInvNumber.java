package com.vishalpaswan.invoiceGen.inputValidationCheck;

import java.time.LocalDate;

public class GenerateInvNumber {
    // generate invoice number
    public static String generateInvoiceNumber(int previousTotalInvoices) {
        if (previousTotalInvoices < 0) {
            throw new IllegalArgumentException("Previous total invoices cannot be negative");
        }
        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear()).substring(2);
        String month = String.format("%02d", now.getMonthValue());
        String sequence = String.format("%03d", previousTotalInvoices + 1);
        return "INV-" + year + month + sequence;
    }
}
