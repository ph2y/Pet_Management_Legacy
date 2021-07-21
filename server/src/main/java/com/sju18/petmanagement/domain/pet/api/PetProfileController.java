package com.sju18.petmanagement.domain.pet.api;

import com.sju18.petmanagement.domain.pet.application.PetProfileService;
import com.sju18.petmanagement.domain.pet.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class PetProfileController {
    private final PetProfileService petInfoService;

    // CREATE
    @PostMapping("/api/pet/profile/create")
    public ResponseEntity<?> createPetProfile(Authentication authentication, @RequestBody PetProfileCreateRequestDto requestDto) {
        return ResponseEntity.ok(petInfoService.createPetProfile(authentication, requestDto));
    }

    // READ
    @PostMapping("/api/pet/profile/fetch")
    public ResponseEntity<?> fetchPetProfile(Authentication authentication) {
        return ResponseEntity.ok(petInfoService.fetchPetProfile(authentication));
    }

    // UPDATE
    @PostMapping("/api/pet/profile/update")
    public ResponseEntity<?> updatePetProfile(Authentication authentication, @RequestBody PetProfileUpdateRequestDto requestDto) {
        return ResponseEntity.ok(petInfoService.updatePetProfile(authentication, requestDto));
    }

    // DELETE
    @PostMapping("/api/pet/profile/delete")
    public ResponseEntity<?> deletePetProfile(Authentication authentication, @RequestBody PetProfileDeleteRequestDto requestDto) {
        return ResponseEntity.ok(petInfoService.deletePetProfile(authentication, requestDto));
    }
}
