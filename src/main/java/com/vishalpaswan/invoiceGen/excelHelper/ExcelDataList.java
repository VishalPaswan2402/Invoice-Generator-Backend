package com.vishalpaswan.invoiceGen.excelHelper;

import com.vishalpaswan.invoiceGen.entity.Invoice;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ExcelDataList {
    private boolean isDatePass(String savedDate) {
        LocalDate storedDate = LocalDate.parse(savedDate);
        LocalDate today = LocalDate.now();
        return storedDate.isBefore(today);
    }

    private ArrayList<ExcelDataInfo> excelData(List<Invoice> invoices) {
        ArrayList<ExcelDataInfo> excelSheetData = new ArrayList<>();
        int count = 1;
        for (Invoice invoice : invoices) {
            ExcelDataInfo excelDataInfo = ExcelDataInfo.builder()
                    .srNo(count)
                    .invoiceNo(invoice.getInvoiceDetails().getInvNumber())
                    .customerName(invoice.getBillingDetails().getName())
                    .customerEmail(invoice.getBillingDetails().getEmail())
                    .customerPhone(invoice.getBillingDetails().getPhone())
                    .customerAddress(invoice.getBillingDetails().getAddress())
                    .billingDate(invoice.getInvoiceDetails().getDate())
                    .dueDate(invoice.getInvoiceDetails().getDueDate())
                    .totalItems(invoice.getItemsDetails().size())
                    .totalCost(invoice.getGrandTotal())
                    .paidAmount(invoice.getPaidAmount())
                    .pendingAmount(invoice.getDueBalance())
                    .status((invoice.isDueClear() || invoice.getDueBalance() == 0) ? "Paid" : isDatePass(invoice.getInvoiceDetails().getDueDate()) ? "Overdue" : "Pending")
                    .build();
            excelSheetData.add(excelDataInfo);

            count++;
        }
        return excelSheetData;
    }

    public ArrayList<ExcelDataInfo> makeDataList(List<Invoice> invoices) {
        try {
            log.info("Generating excel data list...");
            return excelData(invoices);
        } catch (Exception ex) {
            log.error("Something went wrong while generating excel data list. : {}", ex.getMessage());
            return null;
        }
    }

}
