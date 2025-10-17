package com.vishalpaswan.invoiceGen.errorHandling;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
public class ApiError {
    private LocalDateTime timeStamp;
    private String errorMsg;
    private HttpStatus statusCode;

    public ApiError() {
        this.timeStamp = LocalDateTime.now();
    }

    public ApiError(String errorMsg, HttpStatus statusCode) {
        this();
        this.errorMsg = errorMsg;
        this.statusCode = statusCode;
    }
}
