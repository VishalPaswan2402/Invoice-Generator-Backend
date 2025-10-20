package com.vishalpaswan.invoiceGen.mappersUtills;

import com.vishalpaswan.invoiceGen.dto.AllCompanyList;
import com.vishalpaswan.invoiceGen.entity.Companies;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AllCompanyMapper {
    AllCompanyList mapToAllCompany(Companies companies);

    default List<AllCompanyList> mapToAllCompanyList(List<Companies> companies) {
        return companies.stream()
                .map(this::mapToAllCompany)
                .collect(Collectors.toList());
    }
}
