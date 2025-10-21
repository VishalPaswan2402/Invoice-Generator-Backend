package com.vishalpaswan.invoiceGen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphStatsDays {
    private String date;
    private int amount;
}
