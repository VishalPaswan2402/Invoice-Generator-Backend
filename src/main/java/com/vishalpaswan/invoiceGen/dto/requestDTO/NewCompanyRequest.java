package com.vishalpaswan.invoiceGen.dto.requestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewCompanyRequest {
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

}
