package com.sju18.petmanagement.domain.map.place.api;

import com.sju18.petmanagement.domain.map.place.application.PlaceService;
import com.sju18.petmanagement.domain.map.place.dao.Place;
import com.sju18.petmanagement.domain.map.place.dto.*;
import com.sju18.petmanagement.global.common.DtoMetadata;
import com.sju18.petmanagement.global.message.MessageConfig;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@RestController
public class PlaceController {
    private static final Logger logger = LogManager.getLogger();
    private final MessageSource msgSrc = MessageConfig.getMapMessageSource();
    private final PlaceService placeServ;

    // CREATE
    @PostMapping("/api/place/create")
    public ResponseEntity<?> createPlace(@Valid @RequestBody CreatePlaceReqDto reqDto) {
        DtoMetadata dtoMetadata;

        try {
            placeServ.createPlace(reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new CreatePlaceResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.place.create.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new CreatePlaceResDto(dtoMetadata));
    }

    // READ
    @PostMapping("/api/place/fetch")
    public ResponseEntity<?> fetchPlace(@Valid @RequestBody FetchPlaceReqDto reqDto) {
        DtoMetadata dtoMetadata;
        List<Place> placeList;

        try {
            if (reqDto.getId() != null) {
                placeList = new ArrayList<>();
                placeList.add(placeServ.fetchPlaceById(reqDto.getId()));
            } else {
                placeList = placeServ.fetchPlaceByDistance(reqDto);
            }
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new FetchPlaceResDto(dtoMetadata, null));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.place.fetch.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new FetchPlaceResDto(dtoMetadata, placeList));
    }

    // UPDATE
    @PostMapping("/api/place/update")
    public ResponseEntity<?> updatePlace(@Valid @RequestBody UpdatePlaceReqDto reqDto) {
        DtoMetadata dtoMetadata;

        try {
            placeServ.updatePlace(reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new UpdatePlaceResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.place.update.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new UpdatePlaceResDto(dtoMetadata));
    }

    // DELETE
    @PostMapping("/api/place/delete")
    public ResponseEntity<?> deletePlace(@Valid @RequestBody DeletePlaceReqDto reqDto) {
        DtoMetadata dtoMetadata;

        try {
            placeServ.deletePlace(reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new DeletePlaceResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.place.delete.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new DeletePlaceResDto(dtoMetadata));
    }
}
