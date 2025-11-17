package com.vishalpaswan.invoiceGen.mappersUtills;

import com.vishalpaswan.invoiceGen.dto.responseDTO.CompanyResponse;
import com.vishalpaswan.invoiceGen.entity.Companies;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CompanyResponseMapper {
    @Mapping(target = "totalInvoice", ignore = true)
    @Mapping(target = "nextInvoiceNumber", ignore = true)
    CompanyResponse mapToCompanyResponse(Companies companies);
}
