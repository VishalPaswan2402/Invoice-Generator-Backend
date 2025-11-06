package com.vishalpaswan.invoiceGen.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatsSummary {
    //    private Info totalInvoices = new Info();
//    private Info paidInvoices = new Info();
//    private Info unpaidInvoices = new Info();
//    private Info overDueInvoices = new Info();
//    private Info totalSellAmount = new Info();
//
//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    @Builder
//    public static class Info {
//        private String name;
//        private int val;
//        private int idx;
//    }
    private String name;
    private String value;
    private int idx;
}
