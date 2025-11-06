package com.vishalpaswan.invoiceGen.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphStatsMonths {
    private String month;
    private int amount;
}
