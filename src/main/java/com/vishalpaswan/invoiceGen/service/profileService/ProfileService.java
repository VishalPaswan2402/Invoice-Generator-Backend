package com.vishalpaswan.invoiceGen.service.profileService;

import com.vishalpaswan.invoiceGen.service.profileService.profileServiceImp.ProfileDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileDetails profileDetails;

    // get profile details
    public ResponseEntity<?> getProfileDetails(String ownerId) {
        return profileDetails.getProfile(ownerId);
    }
    
}
