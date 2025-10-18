package com.vishalpaswan.invoiceGen.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {
    @Valid
    @NotBlank(message = "Username is required.")
    private String username;

    @Valid
    @NotBlank(message = "Email is required.")
    private String email;

    @Valid
    @NotBlank(message = "Password is required.")
    private String password;

    @Valid
    @NotBlank(message = "Confirm password is required.")
    private String confirmPassword;
}
