package com.sju18.petmanagement.domain.map.place.application;

import com.sju18.petmanagement.domain.map.place.dao.Place;
import com.sju18.petmanagement.domain.map.place.dao.PlaceRepository;
import com.sju18.petmanagement.domain.map.place.dto.CreatePlaceReqDto;
import com.sju18.petmanagement.domain.map.place.dto.DeletePlaceReqDto;
import com.sju18.petmanagement.domain.map.place.dto.FetchPlaceReqDto;
import com.sju18.petmanagement.domain.map.place.dto.UpdatePlaceReqDto;
import com.sju18.petmanagement.global.message.MessageConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@Service
public class PlaceService {
    private final MessageSource msgSrc = MessageConfig.getMapMessageSource();
    private final PlaceRepository placeRepository;
    private final PlacePositionService placePositionServ;

    // CREATE
    @Transactional
    public void createPlace(CreatePlaceReqDto reqDto) {
        // 받은 입력 정보로 새 장소 정보 생성
        Place place = Place.builder()
                .name(reqDto.getName())
                .categoryName(reqDto.getCategoryName())
                .categoryCode(reqDto.getCategoryCode())
                .addressName(reqDto.getAddressName())
                .roadAddressName(reqDto.getRoadAddressName())
                .latitude(reqDto.getLatitude().doubleValue())
                .longitude(reqDto.getLongitude().doubleValue())
                .isOfficial(reqDto.getIsOfficial())
                .averageRating(null)
                .phone(reqDto.getPhone())
                .description(reqDto.getDescription())
                .operationHour(reqDto.getOperationHour())
                .build();

        // save
        placeRepository.save(place);
    }

    // READ
    @Transactional(readOnly = true)
    public List<Place> fetchPlaceByDistance(FetchPlaceReqDto reqDto) {
        Double currentLat = reqDto.getCurrentLat().doubleValue();
        Double currentLong = reqDto.getCurrentLong().doubleValue();
        Double range = reqDto.getRange().doubleValue();
        Double latMin = placePositionServ.calcMinLatForRange(currentLat, range);
        Double latMax = placePositionServ.calcMaxLatForRange(currentLat, range);
        Double longMin = placePositionServ.calcMinLongForRange(currentLat, currentLong, range);
        Double longMax = placePositionServ.calcMaxLongForRange(currentLat, currentLong, range);
        return placeRepository.findAllByDistance(latMin, latMax, longMin, longMax);
    }
    @Transactional(readOnly = true)
    public Place fetchPlaceById(Long placeId) throws Exception {
        return placeRepository.findById(placeId)
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.place.notExists", null, Locale.ENGLISH)
                ));
    }

    // UPDATE
    @Transactional
    public void updatePlace(UpdatePlaceReqDto reqDto) throws Exception {
        Place currentPlace = placeRepository.findById(reqDto.getId())
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.place.notExists", null, Locale.ENGLISH)
                ));

        if (!reqDto.getName().equals(currentPlace.getName())) {
            currentPlace.setName(reqDto.getName());
        }
        if (!reqDto.getCategoryName().equals(currentPlace.getCategoryName())) {
            currentPlace.setCategoryName(reqDto.getCategoryName());
        }
        if (!reqDto.getCategoryCode().equals(currentPlace.getCategoryCode())) {
            currentPlace.setCategoryCode(reqDto.getCategoryCode());
        }
        if (!reqDto.getAddressName().equals(currentPlace.getAddressName())) {
            currentPlace.setAddressName(reqDto.getAddressName());
        }
        if (!reqDto.getRoadAddressName().equals(currentPlace.getRoadAddressName())) {
            currentPlace.setRoadAddressName(reqDto.getRoadAddressName());
        }
        if (reqDto.getLatitude().doubleValue() != currentPlace.getLatitude()) {
            currentPlace.setLatitude(reqDto.getLatitude().doubleValue());
        }
        if (reqDto.getLongitude().doubleValue() != currentPlace.getLongitude()) {
            currentPlace.setLongitude(reqDto.getLongitude().doubleValue());
        }
        if (!reqDto.getIsOfficial().equals(currentPlace.getIsOfficial())) {
            currentPlace.setIsOfficial(currentPlace.getIsOfficial());
        }
        if (!reqDto.getPhone().equals(currentPlace.getPhone())) {
            currentPlace.setPhone(reqDto.getPhone());
        }
        if (!reqDto.getDescription().equals(currentPlace.getDescription())) {
            currentPlace.setDescription(reqDto.getDescription());
        }
        if (!reqDto.getOperationHour().equals(currentPlace.getOperationHour())) {
            currentPlace.setOperationHour(reqDto.getOperationHour());
        }

        // save
        placeRepository.save(currentPlace);
    }

    // DELETE
    public void deletePlace(DeletePlaceReqDto reqDto) throws Exception {
        // 받은 장소 id로 장소 정보 삭제
        Place place = placeRepository.findById(reqDto.getId())
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.place.notExists", null, Locale.ENGLISH)
                ));
        placeRepository.delete(place);
    }
}
