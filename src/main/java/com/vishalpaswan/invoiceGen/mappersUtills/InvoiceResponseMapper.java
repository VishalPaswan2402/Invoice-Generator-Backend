package com.vishalpaswan.invoiceGen.mappersUtills;

import com.vishalpaswan.invoiceGen.dto.InvoiceResponse;
import com.vishalpaswan.invoiceGen.entity.Invoice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InvoiceResponseMapper {
    @Mapping(target = "companyDetails", ignore = true)
    InvoiceResponse mapToResponse(Invoice invoice);

    List<InvoiceResponse.ItemsDetails> mapItems(List<Invoice.ItemsDetails> items);

    InvoiceResponse.BillingDetails mapBilling(Invoice.BillingDetails billing);

    InvoiceResponse.InvoiceDetails mapInvoiceDetails(Invoice.InvoiceDetails details);
}
