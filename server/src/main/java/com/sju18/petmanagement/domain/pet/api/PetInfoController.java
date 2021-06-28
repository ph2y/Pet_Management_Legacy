package com.sju18.petmanagement.domain.pet.api;

import com.sju18.petmanagement.domain.pet.application.PetInfoService;
import com.sju18.petmanagement.domain.pet.dto.PetInfoCreateRequestDto;
import com.sju18.petmanagement.domain.pet.dto.PetInfoDeleteRequestDto;
import com.sju18.petmanagement.domain.pet.dto.PetInfoResponseDTO;
import com.sju18.petmanagement.domain.pet.dto.PetInfoUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class PetInfoController {
    private final PetInfoService petInfoService;

    // CREATE
    @PostMapping("/api/pet/info/create")
    public Long createPetInfo(Authentication authentication, @RequestBody PetInfoCreateRequestDto requestDto) {
        return petInfoService.createPetInfo(authentication, requestDto);
    }

    // READ
    @PostMapping("/api/pet/info/fetch")
    public ResponseEntity<?> fetchPetInfo(Authentication authentication) {
        return ResponseEntity.ok(petInfoService.fetchPetInfo(authentication));
    }

    // UPDATE
    @PostMapping("/api/pet/info/update")
    public Long updatePetInfo(Authentication authentication, @RequestBody PetInfoUpdateRequestDto requestDto) {
        return petInfoService.updatePetInfo(authentication, requestDto);
    }

    // DELETE
    @PostMapping("/api/pet/info/delete")
    public void deletePetInfo(Authentication authentication, @RequestBody PetInfoDeleteRequestDto requestDto) {
        petInfoService.deletePetInfo(authentication, requestDto);
    }
}
