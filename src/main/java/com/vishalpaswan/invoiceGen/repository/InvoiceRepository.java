package com.vishalpaswan.invoiceGen.repository;

import com.vishalpaswan.invoiceGen.entity.Invoice;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface InvoiceRepository extends MongoRepository<Invoice,String> { }
