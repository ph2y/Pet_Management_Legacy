package com.sju18.petmanagement.domain.pet.api;

import com.sju18.petmanagement.domain.pet.application.PetFeedScheduleService;
import com.sju18.petmanagement.domain.pet.dto.PetFeedScheduleCreateRequestDto;
import com.sju18.petmanagement.domain.pet.dto.PetFeedScheduleDeleteRequestDto;
import com.sju18.petmanagement.domain.pet.dto.PetFeedScheduleUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class PetFeedScheduleController {
    private final PetFeedScheduleService petFeedScheduleService;

    // CREATE
    @PostMapping("/api/pet/feed/create")
    public ResponseEntity<?> createPetProfile(Authentication authentication, @RequestBody PetFeedScheduleCreateRequestDto requestDto) {
        return ResponseEntity.ok(petFeedScheduleService.createPetFeedSchedule(authentication, requestDto));
    }

    // READ
    @PostMapping("/api/pet/feed/fetch")
    public ResponseEntity<?> fetchPetProfile(Authentication authentication) {
        return ResponseEntity.ok(petFeedScheduleService.fetchPetFeedSchedule(authentication));
    }

    // UPDATE
    @PostMapping("/api/pet/feed/update")
    public ResponseEntity<?> updatePetProfile(Authentication authentication, @RequestBody PetFeedScheduleUpdateRequestDto requestDto) {
        return ResponseEntity.ok(petFeedScheduleService.updatePetFeedSchedule(authentication, requestDto));
    }

    // DELETE
    @PostMapping("/api/pet/feed/delete")
    public ResponseEntity<?> deletePetProfile(Authentication authentication, @RequestBody PetFeedScheduleDeleteRequestDto requestDto) {
        return ResponseEntity.ok(petFeedScheduleService.deletePetFeedSchedule(authentication, requestDto));
    }
}
