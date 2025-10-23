package com.vishalpaswan.invoiceGen.repository;

import com.vishalpaswan.invoiceGen.entity.Companies;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CompaniesRepository extends MongoRepository<Companies, String> {
    Optional<Companies> findByOwnerId(String ownerId);

    List<Companies> findAllByOwnerId(String ownerId);
}
