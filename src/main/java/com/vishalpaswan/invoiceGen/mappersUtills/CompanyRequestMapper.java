package com.vishalpaswan.invoiceGen.mappersUtills;

import com.vishalpaswan.invoiceGen.dto.requestDTO.NewCompanyRequest;
import com.vishalpaswan.invoiceGen.entity.Companies;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CompanyRequestMapper {
    @Mapping(target = "owner", ignore = true)
    Companies mapToCompanies(NewCompanyRequest newCompanyRequest);
}
