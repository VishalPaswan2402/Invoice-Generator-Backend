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
    private String name;
    private String value;
    private int idx;
}
