package com.vishalpaswan.invoiceGen.entity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "companies")
public class Companies {
    @Id
    private String id;

    private String ownerId;

    @Valid
    @NotBlank(message = "Company name missing.")
    private String companyName;

    @Valid
    @NotBlank(message = "Owner name is missing,")
    private String ownerName;

    @Valid
    @NotBlank(message = "Contact number is missing.")
    private String contact;

    @Valid
    @NotBlank(message = "Address is missing.")
    private String address;

    @Valid
    @NotBlank(message = "Email is missing.")
    private String email;

    @DBRef(lazy = true)
    private Users owner;

    @DBRef(lazy = true)
    private List<Invoice> invoices = new ArrayList<>();

}
