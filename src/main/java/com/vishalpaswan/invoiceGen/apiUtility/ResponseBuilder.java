package com.vishalpaswan.invoiceGen.apiUtility;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseBuilder {
    public static <T> ResponseEntity<ApiResponse<T>> success(HttpStatus status, String message, T data) {
        return ResponseEntity.status(status)
                .body(
                        ApiResponse.<T>builder()
                                .success(true)
                                .message(message)
                                .data(data)
                                .build()
                );
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(
                        ApiResponse.<T>builder()
                                .success(false)
                                .message(message)
                                .data(null)
                                .build()
                );
    }


    public static <T> ResponseEntity<PageResponse<T>> success(HttpStatus status, String message, T data, int page, int size, int totalPages, long totalElements) {
        return ResponseEntity.status(status)
                .body(
                        PageResponse.<T>builder()
                                .success(true)
                                .message(message)
                                .data(data)
                                .page(page)
                                .size(size)
                                .totalPages(totalPages)
                                .totalElements(totalElements)
                                .build()
                );
    }

}
