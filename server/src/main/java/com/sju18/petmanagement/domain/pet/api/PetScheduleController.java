package com.sju18.petmanagement.domain.pet.api;

import com.sju18.petmanagement.domain.pet.application.PetScheduleService;
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
public class PetScheduleController {
    private static final Logger logger = LogManager.getLogger();
    private final MessageSource msgSrc = MessageConfig.getPetMessageSource();
    private final PetScheduleService petScheduleServ;

    // CREATE
    @PostMapping("/api/pet/feed/create")
    public ResponseEntity<?> createPetProfile(Authentication auth, @Valid @RequestBody PetScheduleCreateReqDto reqDto) {
        DtoMetadata dtoMetadata;

        try {
            petScheduleServ.createPetSchedule(auth, reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new PetScheduleCreateResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.petSchedule.create.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new PetScheduleCreateResDto(dtoMetadata));
    }

    // READ
    @PostMapping("/api/pet/feed/fetch")
    public ResponseEntity<?> fetchPetProfile(Authentication auth) {
        return ResponseEntity.ok(petScheduleServ.fetchPetSchedule(auth));
    }

    // UPDATE
    @PostMapping("/api/pet/feed/update")
    public ResponseEntity<?> updatePetProfile(Authentication auth, @RequestBody PetScheduleUpdateReqDto reqDto) {
        return ResponseEntity.ok(petScheduleServ.updatePetSchedule(auth, reqDto));
    }

    // DELETE
    @PostMapping("/api/pet/feed/delete")
    public ResponseEntity<?> deletePetProfile(Authentication auth, @RequestBody PetScheduleDeleteReqDto reqDto) {
        return ResponseEntity.ok(petScheduleServ.deletePetSchedule(auth, reqDto));
    }
}
