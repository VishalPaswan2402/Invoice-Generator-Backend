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
public class OtpRequest {
    @Valid
    @NotBlank(message = "user ID missing.")
    private String id;

    @Valid
    @NotBlank(message = "OTP missing.")
    private String otp;
}
