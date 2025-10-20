package com.vishalpaswan.invoiceGen.mappersUtills;

import com.vishalpaswan.invoiceGen.dto.CompanyResponse;
import com.vishalpaswan.invoiceGen.entity.Companies;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CompanyResponseMapper {
    @Mapping(target = "invNumber", ignore = true)
    CompanyResponse mapToCompanyResponse(Companies companies);
}
