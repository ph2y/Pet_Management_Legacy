package com.sju18.petmanagement.domain.pet.schedule.api;

import com.sju18.petmanagement.domain.pet.schedule.application.PetScheduleService;
import com.sju18.petmanagement.domain.pet.schedule.dao.PetSchedule;
import com.sju18.petmanagement.domain.pet.schedule.dto.*;
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
    public ResponseEntity<?> createPetSchedule(Authentication auth, @Valid @RequestBody CreatePetScheduleReqDto reqDto) {
        DtoMetadata dtoMetadata;

        try {
            petScheduleServ.createPetSchedule(auth, reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new CreatePetScheduleResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.petSchedule.create.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new CreatePetScheduleResDto(dtoMetadata));
    }

    // READ
    @PostMapping("/api/pet/schedule/fetch")
    public ResponseEntity<?> fetchPetSchedule(Authentication auth, @Valid @RequestBody FetchPetScheduleReqDto reqDto) {
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
            return ResponseEntity.status(400).body(new FetchPetScheduleResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.petSchedule.fetch.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new FetchPetScheduleResDto(dtoMetadata, petScheduleList));
    }

    // UPDATE
    @PostMapping("/api/pet/schedule/update")
    public ResponseEntity<?> updatePetSchedule(Authentication auth, @Valid @RequestBody UpdatePetScheduleReqDto reqDto) {
        DtoMetadata dtoMetadata;
        try {
            petScheduleServ.updatePetSchedule(auth, reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new UpdatePetScheduleResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.petSchedule.update.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new UpdatePetScheduleResDto(dtoMetadata));
    }

    // DELETE
    @PostMapping("/api/pet/schedule/delete")
    public ResponseEntity<?> deletePetSchedule(Authentication auth, @Valid @RequestBody DeletePetScheduleReqDto reqDto) {
        DtoMetadata dtoMetadata;
        try {
            petScheduleServ.deletePetSchedule(auth, reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new DeletePetScheduleResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.petSchedule.delete.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new DeletePetScheduleResDto(dtoMetadata));
    }
}
