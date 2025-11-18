package com.vishalpaswan.invoiceGen.dto.requestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditCompanyRequest {
    @Valid
    @NotBlank(message = "Owner name is missing")
    private String ownerName;

    @Valid
    @NotBlank(message = "Email is missing")
    @Email(message = "Invalid email")
    private String email;

    @Valid
    @NotBlank(message = "Contact is missing")
    private String contact;

    @Valid
    @NotBlank(message = "Address is missing")
    private String address;
}
