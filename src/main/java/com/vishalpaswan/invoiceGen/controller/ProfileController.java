package com.vishalpaswan.invoiceGen.controller;

import com.vishalpaswan.invoiceGen.security.AuthUtils;
import com.vishalpaswan.invoiceGen.security.Authorize;
import com.vishalpaswan.invoiceGen.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invoice-gen/api")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ProfileController {

    private final ProfileService profileService;
    private final AuthUtils authUtils;
    private final Authorize authorize;

    @GetMapping("/{ownerId}/profile")
    public ResponseEntity<?> getProfileDetails(@PathVariable String ownerId, @RequestHeader("Authorization") String authHeader) {
//        if (!authorize.isUserAuthorize(authHeader, ownerId)) {
//            return ResponseBuilder.error(HttpStatus.FORBIDDEN, "Access denied: you are not allowed to view this profile.");
//        }
        ResponseEntity<?> authResult = authorize.isAuthorizes(authHeader, ownerId);
        return authResult != null ? authResult : profileService.getProfileDetails(ownerId);
    }
    
}
