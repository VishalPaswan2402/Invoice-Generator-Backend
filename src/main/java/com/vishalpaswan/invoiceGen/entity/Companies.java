package com.vishalpaswan.invoiceGen.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "companies")
public class Companies {
    @Id
    private String id;
    private String companyName;
    private String ownerName;
    private String contact;
    private String address;
    private String email;

    @DBRef(lazy = true)
    @JsonIgnore
    @ToString.Exclude
    private Users owner;

}
