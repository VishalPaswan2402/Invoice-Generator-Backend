package com.vishalpaswan.invoiceGen.repository;

import com.vishalpaswan.invoiceGen.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface InvoiceRepository extends MongoRepository<Invoice, String> {
    Page<Invoice> findByCompanyId(String companyId, Pageable pageable);

    List<Invoice> findByCompanyId(String companyId);

    List<Invoice> findByCompanyId(String companyId, Sort sort);
}
