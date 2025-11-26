package com.vishalpaswan.invoiceGen.apiUtility;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private int page;
    private int size;
    private int totalPages;
    private long totalElements;
}
