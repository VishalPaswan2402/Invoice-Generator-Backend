package com.vishalpaswan.invoiceGen.repository;

import com.vishalpaswan.invoiceGen.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface InvoiceRepository extends MongoRepository<Invoice, String> {
    Page<Invoice> findByCompanyId(String companyId, Pageable pageable);

    List<Invoice> findByCompanyId(String companyId);

    List<Invoice> findByCompanyId(String companyId, Sort sort);

    List<Invoice> findByInvoiceDetailsDate(String date);

    @Query("{ 'invoiceDetails.date': { $gte: ?0, $lte: ?1 }, 'companyId': ?2 }")
    List<Invoice> findInvoiceDetailsBetweenDatesAndCompany(String start, String end, String companyId);
}
