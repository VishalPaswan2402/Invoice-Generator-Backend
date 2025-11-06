package com.vishalpaswan.invoiceGen.dto.requestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    @Valid
    @NotBlank(message = "Username is missing.")
    private String username;

    @Valid
    @NotBlank(message = "Password is missing.")
    private String password;

}
