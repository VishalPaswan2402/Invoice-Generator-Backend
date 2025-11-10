package com.vishalpaswan.invoiceGen.excelHelper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExcelDataInfo {
    private int srNo;
    private String invoiceNo;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String customerAddress;
    private String billingDate;
    private String dueDate;
    private int totalItems;
    private int totalCost;
    private int paidAmount;
    private int pendingAmount;
    private String status;
}
