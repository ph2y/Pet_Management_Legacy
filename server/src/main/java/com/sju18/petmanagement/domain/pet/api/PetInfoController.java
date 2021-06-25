package com.sju18.petmanagement.domain.pet.api;

import com.sju18.petmanagement.domain.pet.application.PetInfoService;
import com.sju18.petmanagement.domain.pet.dto.PetInfoRequestDTO;
import com.sju18.petmanagement.domain.pet.dto.PetInfoResponseDTO;
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

    @PostMapping("/api/pet/info")
    public ResponseEntity<?> fetchPetInfo(Authentication authentication, @RequestBody PetInfoRequestDTO petInfoRequestDTO) {
        PetInfoResponseDTO petInfoResponseDTO = petInfoService.fetchPetInfo(authentication);
        return ResponseEntity.ok(petInfoResponseDTO);
    }
}
