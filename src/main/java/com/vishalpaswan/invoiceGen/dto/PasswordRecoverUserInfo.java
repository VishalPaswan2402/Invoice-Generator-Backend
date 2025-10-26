package com.vishalpaswan.invoiceGen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordRecoverUserInfo {
    private String id;
    private String username;
    private String email;
}
