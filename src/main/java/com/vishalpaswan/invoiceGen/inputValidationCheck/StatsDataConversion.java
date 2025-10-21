package com.vishalpaswan.invoiceGen.inputValidationCheck;

import com.vishalpaswan.invoiceGen.entity.Invoice;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StatsDataConversion {
    public static String changeDateFormat(String currDate) {
        LocalDate date = LocalDate.parse(currDate);
        DateTimeFormatter dateFormate = DateTimeFormatter.ofPattern("dd MMM");
        return date.format(dateFormate);
    }

    public static int findTotalAmount(List<Invoice> invoiceList) {
        int amount = 0;
        for (Invoice invoice : invoiceList) {
            amount += invoice.getGrandTotal();
        }
        return amount;
    }
}
