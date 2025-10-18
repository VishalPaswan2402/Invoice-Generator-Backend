package com.vishalpaswan.invoiceGen.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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

    @Valid
    @Indexed(unique = true)
    @NotBlank(message = "Username is required")
    private String username;

    @Valid
    @NotBlank(message = "Email is required")
    private String email;

    @Valid
    @NotBlank(message = "Password is required")
    private String password;

    @Builder.Default
    private int totalInvoices = 0;

    @Builder.Default
    private int totalCompany = 0;

    @DBRef(lazy = true)
    @JsonIgnore
    @ToString.Exclude
    private Companies company;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }
}
