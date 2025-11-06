package com.vishalpaswan.invoiceGen.mappersUtills;

import com.vishalpaswan.invoiceGen.dto.requestDTO.InvoiceRequest;
import com.vishalpaswan.invoiceGen.entity.Invoice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InvoiceRequestMapper {
    @Mapping(target = "grandTotal", ignore = true)
    @Mapping(target = "dueBalance", ignore = true)
    @Mapping(target = "company", ignore = true)
    Invoice mapToInvoice(InvoiceRequest invoiceRequest);

    Invoice.BillingDetails mapBillingDetails(InvoiceRequest.BillingDetails dto);

    Invoice.InvoiceDetails mapInvoiceDetails(InvoiceRequest.InvoiceDetails dto);

    List<Invoice.ItemsDetails> mapItems(List<InvoiceRequest.ItemsDetails> dtoList);
}
