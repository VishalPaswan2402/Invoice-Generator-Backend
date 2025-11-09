package com.vishalpaswan.invoiceGen.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MailQueueItems {
    private String sender;
    private String receiver;
    private String companyName;
    private String invoiceUrl;
    private int maxReTry = 0;
}
