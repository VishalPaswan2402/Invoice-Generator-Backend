package com.vishalpaswan.invoiceGen.repository;

import com.vishalpaswan.invoiceGen.entity.Users;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

//@Repository
public interface UserRepository extends MongoRepository<Users,String> {
    boolean existsByUsername(String username);
//    Users findByUsernameAndPassword(String username,String password);
    Users findByUsername(String username);
}
