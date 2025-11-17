package com.vishalpaswan.invoiceGen.mappersUtills;

import com.vishalpaswan.invoiceGen.dto.responseDTO.AllCompanyList;
import com.vishalpaswan.invoiceGen.entity.Companies;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AllCompanyMapper {
    AllCompanyList mapToAllCompany(Companies companies);

    default AllCompanyList mapToAllCompanyList(Companies companies) {
//        return companies.stream()
//                .map(this::mapToAllCompany)
//                .collect(Collectors.toList());
        return mapToAllCompany(companies);
    }
}
