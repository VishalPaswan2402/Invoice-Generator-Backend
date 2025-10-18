package com.vishalpaswan.invoiceGen.mappersUtills;

import com.vishalpaswan.invoiceGen.dto.CompanyResponse;
import com.vishalpaswan.invoiceGen.entity.Companies;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CompanyResponseMapper {
    CompanyResponse mapToCompanyResponse(Companies companies);
}
