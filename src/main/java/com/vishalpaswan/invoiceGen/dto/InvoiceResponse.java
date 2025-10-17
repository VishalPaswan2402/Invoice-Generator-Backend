package com.vishalpaswan.invoiceGen.dto;

import com.vishalpaswan.invoiceGen.entity.Companies;
import com.vishalpaswan.invoiceGen.entity.Invoice;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class InvoiceResponse {
    private Companies companyDetails;
    private Invoice invoiceDetails;
}
