package com.vishalpaswan.invoiceGen.dto.requestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecoverPasswordRequest {
    @Valid
    @NotBlank(message = "Username is missing.")
    private String username;

    @Valid
    @NotBlank(message = "Email is missing")
    private String email;
}
