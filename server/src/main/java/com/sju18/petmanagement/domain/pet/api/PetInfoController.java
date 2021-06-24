package com.sju18.petmanagement.domain.pet.api;

import com.sju18.petmanagement.domain.pet.application.PetInfoService;
import com.sju18.petmanagement.domain.pet.dto.PetInfoRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class PetInfoController {
    private PetInfoService petInfoService;

    @PostMapping("/api/account/petinfo")
    public ResponseEntity<?> fetchPetInfo(Authentication authentication, @RequestBody PetInfoRequestDTO petInfoRequestDTO) {
        return ResponseEntity.ok(new petInfoService.fetchPetInfo(authentication));
    }
}
