package com.sju18.petmanagement.domain.pet.api;

import com.sju18.petmanagement.domain.pet.application.PetProfileService;
import com.sju18.petmanagement.domain.pet.dto.*;
import com.sju18.petmanagement.global.common.DtoMetadata;
import com.sju18.petmanagement.global.message.MessageConfig;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Locale;

@RequiredArgsConstructor
@RestController
public class PetProfileController {
    private static final Logger logger = LogManager.getLogger();
    private final MessageSource msgSrc = MessageConfig.getPetMessageSource();
    private final PetProfileService petServ;

    // CREATE
    @PostMapping("/api/pet/profile/create")
    public ResponseEntity<?> createPetProfile(Authentication auth, @Valid @RequestBody PetCreateReqDto reqDto) {
        DtoMetadata dtoMetadata;

        try {
            petServ.createPet(auth, reqDto)
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new PetCreateResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.pet.create.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new PetCreateResDto(dtoMetadata));
    }

    // READ
    @PostMapping("/api/pet/profile/fetch")
    public ResponseEntity<?> fetchPet(Authentication auth) {
        return ResponseEntity.ok(petServ.fetchPet(auth));
    }

    // UPDATE
    @PostMapping("/api/pet/profile/update")
    public ResponseEntity<?> updatePet(Authentication auth, @RequestBody PetUpdateReqDto reqDto) {
        return ResponseEntity.ok(petServ.updatePet(auth, reqDto));
    }

    // DELETE
    @PostMapping("/api/pet/profile/delete")
    public ResponseEntity<?> deletePet(Authentication auth, @RequestBody PetDeleteReqDto reqDto) {
        return ResponseEntity.ok(petServ.deletePet(auth, reqDto));
    }
}