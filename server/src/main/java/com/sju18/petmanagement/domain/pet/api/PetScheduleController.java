package com.sju18.petmanagement.domain.pet.api;

import com.sju18.petmanagement.domain.pet.application.PetScheduleService;
import com.sju18.petmanagement.domain.pet.dao.PetSchedule;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@RestController
public class PetScheduleController {
    private static final Logger logger = LogManager.getLogger();
    private final MessageSource msgSrc = MessageConfig.getPetMessageSource();
    private final PetScheduleService petScheduleServ;

    // CREATE
    @PostMapping("/api/pet/schedule/create")
    public ResponseEntity<?> createPetSchedule(Authentication auth, @Valid @RequestBody PetScheduleCreateReqDto reqDto) {
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
    @PostMapping("/api/pet/schedule/fetch")
    public ResponseEntity<?> fetchPetSchedule(Authentication auth, @Valid @RequestBody PetScheduleFetchReqDto reqDto) {
        DtoMetadata dtoMetadata;
        final List<PetSchedule> petScheduleList;

        try {
            if (reqDto.getId() != null) {
                petScheduleList = new ArrayList<>();
                petScheduleList.add(petScheduleServ.fetchPetScheduleById(reqDto.getId()));
            } else {
                petScheduleList = petScheduleServ.fetchPetScheduleList(auth);
            }
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new PetScheduleFetchResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.petSchedule.fetch.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new PetScheduleFetchResDto(dtoMetadata, petScheduleList));
    }

    // UPDATE
    @PostMapping("/api/pet/schedule/update")
    public ResponseEntity<?> updatePetSchedule(Authentication auth, @Valid @RequestBody PetScheduleUpdateReqDto reqDto) {
        DtoMetadata dtoMetadata;
        try {
            petScheduleServ.updatePetSchedule(auth, reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new PetScheduleUpdateResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.petSchedule.update.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new PetScheduleUpdateResDto(dtoMetadata));
    }

    // DELETE
    @PostMapping("/api/pet/schedule/delete")
    public ResponseEntity<?> deletePetSchedule(Authentication auth, @Valid @RequestBody PetScheduleDeleteReqDto reqDto) {
        DtoMetadata dtoMetadata;
        try {
            petScheduleServ.deletePetSchedule(auth, reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new PetScheduleDeleteResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.petSchedule.delete.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new PetScheduleDeleteResDto(dtoMetadata));
    }
}
