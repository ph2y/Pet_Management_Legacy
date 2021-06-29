package com.sju18.petmanagement.domain.place.api;

import com.sju18.petmanagement.domain.place.application.PlaceMarkerService;
import com.sju18.petmanagement.domain.place.dto.PlaceMarkerCreateRequestDto;
import com.sju18.petmanagement.domain.place.dto.PlaceMarkerDeleteRequestDto;
import com.sju18.petmanagement.domain.place.dto.PlaceMarkerUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class PlaceMarkerController {
    private final PlaceMarkerService placeMarkerService;

    // CREATE
    @PostMapping("/api/place/marker/create")
    public Long createPetInfo(Authentication authentication, @RequestBody PlaceMarkerCreateRequestDto requestDto) {
        return placeMarkerService.createPlaceMarker(authentication, requestDto);
    }

    // READ
    @PostMapping("/api/place/marker/fetch")
    public ResponseEntity<?> fetchPetInfo(Authentication authentication) {
        return ResponseEntity.ok(placeMarkerService.fetchPlaceMarker(authentication));
    }

    // UPDATE
    @PostMapping("/api/place/marker/update")
    public Long updatePetInfo(Authentication authentication, @RequestBody PlaceMarkerUpdateRequestDto requestDto) {
        return placeMarkerService.updatePlaceMarker(authentication, requestDto);
    }

    // DELETE
    @PostMapping("/api/place/marker/delete")
    public void deletePetInfo(Authentication authentication, @RequestBody PlaceMarkerDeleteRequestDto requestDto) {
        placeMarkerService.deletePlaceMarker(authentication, requestDto);
    }
}
