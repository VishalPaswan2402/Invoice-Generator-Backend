package com.vishalpaswan.invoiceGen.security;

import com.vishalpaswan.invoiceGen.apiUtility.ResponseBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Authorize {
    @Autowired
    private AuthUtils authUtils;

    private int isUserAuthorize(String token, String userId) {
        try {
            if (token == null || !token.startsWith("Bearer ") || token.isBlank()) {
                return 0; // null token
            }
            String[] tokenArray = token.split(" ");
            String idFromToken = authUtils.getUserIdFromRequestToken(tokenArray[1]);
            if (idFromToken == null) return 3; // error
            return userId.equals(idFromToken) ? 1 : 2; // 1 -> true , 2 -> false
        } catch (Exception ex) {
            log.error("Error validating token: {}", ex.getMessage());
            return 3; // error
        }
    }

    public ResponseEntity<?> isAuthorizes(String token, String userId) {
        int authResult = isUserAuthorize(token, userId);
        return switch (authResult) {
            case 0 -> ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Missing or invalid token.");
            case 1 -> null;
            case 2 ->
                    ResponseBuilder.error(HttpStatus.FORBIDDEN, "Access denied: you are not authorized to perform this action.");
            case 3 ->
                    ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred during authorization.");
            default ->
                    ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error. Please try again later.");
        };
    }
    
}
