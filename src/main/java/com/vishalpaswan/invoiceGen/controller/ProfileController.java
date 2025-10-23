package com.vishalpaswan.invoiceGen.controller;

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

    @GetMapping("/{ownerId}/profile")
    public ResponseEntity<?> getProfileDetails(@PathVariable String ownerId) {
        return profileService.getProfileDetails(ownerId);
    }
}
