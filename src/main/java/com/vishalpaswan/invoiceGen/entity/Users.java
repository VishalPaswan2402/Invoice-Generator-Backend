package com.vishalpaswan.invoiceGen.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "users")
public class Users implements UserDetails {
    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    private String email;

    private String password;

    @Builder.Default
    private int totalInvoices = 0;

    @Builder.Default
    private int totalCompany = 0;

    @DBRef(lazy = true)
    @JsonIgnore
    @ToString.Exclude
    private Companies companies;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }
}
